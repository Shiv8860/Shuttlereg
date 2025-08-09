@echo off
echo Setting up Git repository for ShuttleReg...

REM Initialize git repository if not already done
if not exist .git (
    git init
    echo Repository initialized.
)

REM Add remote origin (replace if already exists)
git remote remove origin 2>nul
git remote add origin https://github.com/Shiv8860/Shuttlereg.git
echo Remote origin added.

REM Add all files to staging
git add .
echo Files added to staging.

REM Commit with message
git commit -m "Initial commit: Complete ShuttleReg Android project with documentation"
echo Changes committed.

REM Push to GitHub (assuming main branch)
git branch -M main
git push -u origin main
echo Project pushed to GitHub successfully!

pause