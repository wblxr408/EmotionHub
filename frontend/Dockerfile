# 前端Dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制package文件
COPY package*.json ./

# 构建阶段需要 devDependencies（vite、vue-tsc 等）
RUN npm ci

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 复制构建产物到nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制nginx配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
