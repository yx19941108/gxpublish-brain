@echo off
git rm -r -f --cached .
git add .
git commit --amend --no-edit
git push -f origin main
