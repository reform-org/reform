FROM node AS build
WORKDIR /app
COPY package*.json tsconfig.json ./
RUN npm install
COPY src src
RUN npm run build

FROM node AS run
COPY --from=build /app/dist /app
WORKDIR /app
COPY package*.json ./
COPY public public
RUN npm install
RUN find . -type d -name "node_modules" -prune -o -type f -print
CMD ["node", "./index.js"]
