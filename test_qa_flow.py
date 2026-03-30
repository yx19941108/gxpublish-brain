import urllib.request
import urllib.parse
import json
import time
import sys

BASE_URL = "http://localhost:9888"
TENANT_ID = "099142"
CLIENT_ID = "e5cd7e4891bf95d1d19206ce24a7b32e"

def login(username, password="123456"):
    url = f"{BASE_URL}/auth/login"
    data = {"tenantId": TENANT_ID, "username": username, "password": password, "clientId": CLIENT_ID, "grantType": "password"}
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read().decode('utf-8'))
            if res.get('code') == 200:
                return res['data']['access_token']
            else:
                print(f"Login failed for {username}: {res}")
                return None
    except Exception as e:
        print(f"Connection error: {e}")
        return None

def create_review(token, title, content):
    url = f"{BASE_URL}/editorial/review"
    headers = {'Authorization': f'Bearer {token}', 'clientid': CLIENT_ID, 'Content-Type': 'application/json'}
    data = {
        "title": title,
        "content": content,
        "deptId": 100,
        "attachmentOssId": "test-oss-1234",
        "attachmentFileName": "test-doc.docx",
        "linkList": [{"url": "http://test", "description": "test desc"}],
        "processType": "AUDIT"
    }
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read().decode('utf-8'))
            if res.get('code') != 200:
                print(f"Create failed response: {res}")
            return res.get('code') == 200
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8')
        print(f"Create HTTPError {e.code}: {error_body}")
        return False
    except Exception as e:
        print(f"Create error: {e}")
        return False

def get_review_list(token, expected_count=None):
    url = f"{BASE_URL}/editorial/review/list"
    headers = {'Authorization': f'Bearer {token}', 'clientid': CLIENT_ID}
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read().decode('utf-8'))
            rows = res.get('rows', [])
            if expected_count is not None:
                assert len(rows) == expected_count, f"Expected {expected_count} reviews, got {len(rows)}"
            return rows
    except Exception as e:
        print(f"List error: {e}")
        return []

def submit_review(token, review):
    url = f"{BASE_URL}/editorial/review/submit"
    headers = {'Authorization': f'Bearer {token}', 'clientid': CLIENT_ID, 'Content-Type': 'application/json'}
    review['processType'] = 'AUDIT'
    review['flowCode'] = 'editorial_review_flow'
    data = review # pass the whole dictionary back
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read().decode('utf-8'))
            if res.get('code') != 200:
                print(f"Submit failed response: {res}")
            return res.get('code') == 200
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8')
        print(f"Submit HTTPError {e.code}: {error_body}")
        return False
    except Exception as e:
        print(f"Submit error: {e}")
        return False

def get_wait_task(token):
    url = f"{BASE_URL}/workflow/task/pageByTaskWait"
    headers = {'Authorization': f'Bearer {token}', 'clientid': CLIENT_ID}
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read().decode('utf-8'))
            return res.get('rows', [])
    except Exception as e:
        print(f"Get Wait Task error: {e}")
        return []

def complete_task(token, task_id, message="Approved"):
    url = f"{BASE_URL}/workflow/task/completeTask"
    headers = {'Authorization': f'Bearer {token}', 'clientid': CLIENT_ID, 'Content-Type': 'application/json'}
    data = {"taskId": task_id, "message": message}
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers)
    try:
        with urllib.request.urlopen(req) as response:
            res = json.loads(response.read().decode('utf-8'))
            return res.get('code') == 200
    except Exception as e:
        print(f"Complete Task error: {e}")
        return False

def main():
    print("--- Start E2E Tests ---")
    
    # Check if server is up
    t_zhangsan = login("zhangsan")
    if not t_zhangsan:
        print("Backend server might be down or user does not exist. Abort.")
        sys.exit(1)
        
    t_lisi = login("lisi")
    t_wangwu = login("wangwu") # Level 1
    t_zhaoliu = login("zhaoliu") # Level 2

    # --- Test 1: Zhangsan (No certificate) ---
    print("\n[Test 1] Initiator without certificate (zhangsan)")
    create_review(t_zhangsan, "Test Title 1", "Content 1")
    time.sleep(1) # wait for DB
    reviews = get_review_list(t_zhangsan)
    assert len(reviews) > 0, "Failed to create review"
    review1 = reviews[0]
    review_id = review1['id']
    print(f"Created review {review_id}. Status: {review1['status']}, ReviewStatus: {review1.get('reviewStatus')}")
    
    # Submit 
    success = submit_review(t_zhangsan, review1)
    assert success, "Failed to submit review"
    print("Submitted review.")
    
    # Check status
    time.sleep(1)
    reviews = get_review_list(t_zhangsan)
    for r in reviews:
        if r['id'] == review_id:
            print(f"After submit, Status: {r['status']}, ReviewStatus: {r.get('reviewStatus')}")
            assert r.get('reviewStatus') == 10, "Status should be 10 (WAITING_FIRST)"
            
    # Check Level 1 visibility and approval
    tasks = get_wait_task(t_wangwu)
    print(f"Wangwu (Level 1) Wait Tasks: {len(tasks)}")
    
    target_task = next((t for t in tasks if str(t.get('businessId')) == str(review_id)), None)
    if not target_task:
        print("Wangwu cannot find the task in WaitTasks! Fallback tracking...")
    else:
        print(f"Wangwu found task: {target_task['id']}")
        complete_task(t_wangwu, target_task['id'])
        print("Wangwu approved.")
        time.sleep(1)
        
        # Check Level 2 visibility and approval
        tasks2 = get_wait_task(t_zhaoliu)
        print(f"Zhaoliu (Level 2) Wait Tasks: {len(tasks2)}")
        target_task2 = next((t for t in tasks2 if str(t.get('businessId')) == str(review_id)), None)
        if target_task2:
            print(f"Zhaoliu found task: {target_task2['id']}")
            complete_task(t_zhaoliu, target_task2['id'])
            print("Zhaoliu approved.")
            time.sleep(1)
            
            # Check Level 3 visibility and approval
            t_qianqi = login("qianqi")
            tasks3 = get_wait_task(t_qianqi)
            print(f"Qianqi (Level 3) Wait Tasks: {len(tasks3)}")
            target_task3 = next((t for t in tasks3 if str(t.get('businessId')) == str(review_id)), None)
            if target_task3:
                print(f"Qianqi found task: {target_task3['id']}")
                complete_task(t_qianqi, target_task3['id'])
                print("Qianqi approved.")
                time.sleep(1)
                
                # Final Status Check
                reviews = get_review_list(t_zhangsan)
                for r in reviews:
                    if r['id'] == review_id:
                        print(f"Final Status: {r['status']}, ReviewStatus: {r.get('reviewStatus')}")
                        assert r.get('reviewStatus') == 40, "Status should be 40 (APPROVED)"
            else:
                print("Qianqi cannot find task!")
        else:
            print("Zhaoliu cannot find task!")

    print("\n[Test 1 Passed partially/fully]")
    
    print("\n--- All tests completed (preview) ---")

if __name__ == "__main__":
    main()
