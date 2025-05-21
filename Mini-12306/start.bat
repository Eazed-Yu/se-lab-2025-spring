@echo off
echo 启动 Mini-12306 演示系统...
echo.

REM 启动后端服务
cd backend
start cmd /k "echo 正在启动后端服务... & mvn spring-boot:run"

REM 给后端一些启动时间
echo 等待后端服务启动...
timeout /t 5 /nobreak > nul

REM 启动前端服务
cd ../frontend
start cmd /k "echo 正在启动前端服务... & npm run start"

echo.
echo Mini-12306 演示系统正在启动...
echo 请等待片刻，前端将自动在浏览器中打开
echo.
echo 后端API: http://localhost:8080/api
echo 前端界面: http://localhost:3000
echo.
echo 按任意键退出启动脚本...
pause > nul
