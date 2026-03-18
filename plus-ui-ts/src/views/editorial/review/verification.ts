import type { ReviewVerificationScenario } from './model';

export const REVIEW_FRONTEND_VERIFICATION_MATRIX: ReviewVerificationScenario[] = [
  {
    key: 'page-detail-contract',
    title: 'Gate 2 显式 contract 对齐',
    route: 'GET /editorial/review/list + GET /editorial/review/{id}',
    evidence: 'page 返回 canEdit/user/dept；detail 返回 historyList/approvalContext',
    notes: '不再依赖 userId/userName/deptName 和 history 二次拼装'
  },
  {
    key: 'ia-index-form-detail',
    title: 'Gate 3 IA 切换',
    route: '/editorial/review -> /editorial/review/form -> /editorial/review/detail',
    evidence: '列表新增/修改进入 form，详情/审批进入 detail shell',
    notes: '确认 reviewEdit 不再作为主入口'
  },
  {
    key: 'legacy-fallback',
    title: 'Gate 6 旧页 fallback 退场顺序',
    route: '/editorial/review/reviewEdit?id=<id>&type=view',
    evidence: '旧路径自动 replace 到 form/detail 新路由',
    notes: '保留 fallback，不再承载主逻辑'
  },
  {
    key: 'resubmit-action',
    title: 'BACK 独立 resubmit action',
    route: '/editorial/review/form?id=<id>&type=update',
    evidence: 'BACK 记录主按钮文案为“再次提交”，真实请求命中独立 resubmit action',
    notes: '不再复用旧 submit 语义'
  },
  {
    key: 'waiting-shell',
    title: '审批壳 WAITING_* 兼容',
    route: '/editorial/review/detail?id=<id>&type=approval&taskId=<taskId>',
    evidence: 'approvalButton 对 WAITING / WAITING_FIRST / WAITING_SECOND / WAITING_FINAL 显示审批入口',
    notes: '不改共享组件的非 editorial 使用方式'
  },
  {
    key: 'lint-build',
    title: '阶段完成静态验证',
    route: 'pnpm eslint + pnpm build:prod',
    evidence: '前端 lint/build 通过',
    notes: '本轮环境若缺少 node_modules，需要先补依赖再执行'
  }
];
