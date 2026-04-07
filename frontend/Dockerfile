# Build Vite + React app, serve static assets with nginx
FROM node:20-alpine AS builder
WORKDIR /app

COPY src/package.json ./
RUN npm install

COPY src/ ./
RUN npm run build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
