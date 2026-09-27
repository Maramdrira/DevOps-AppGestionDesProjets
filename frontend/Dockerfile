# ---- Stage 1: Build Angular ----
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build -- --configuration production || npm run build

# ---- Stage 2: Serve with Nginx ----
FROM nginx:alpine
RUN rm -rf /usr/share/nginx/html/*
COPY --from=build /app/dist /tmp/dist
RUN if [ -d /tmp/dist/browser ]; then \
      cp -r /tmp/dist/browser/* /usr/share/nginx/html/; \
    else \
      cp -r /tmp/dist/*/browser/* /usr/share/nginx/html/ 2>/dev/null || \
      cp -r /tmp/dist/* /usr/share/nginx/html/; \
    fi
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
