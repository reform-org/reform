import sqlite3 from 'sqlite3';

export class db {
  static instance = new sqlite3.Database(process.env.VITE_SERVER_DB_FILE || './discovery.db');

  static get = (sql: string, ...params: string[]) => new Promise<any>((resolve, reject) => {
    db.instance.get(sql, ...params, (err: string, rows: object) => {
      if (err) {
        console.log(err);
        return reject(err);
      }
      else return resolve(rows);
    });
  });

  static all = (sql: string, ...params: string[]) => new Promise<any[]>((resolve, reject) => {
    db.instance.all(sql, ...params, (err: string, rows: object[]) => {
      if (err) {
        console.log(err);
        return reject(err);
      }
      else return resolve(rows);
    });
  });

  static init = () => {
    db.instance.exec(`
    CREATE TABLE IF NOT EXISTS users (id VARCHAR(255) PRIMARY KEY, name VARCHAR(255), password VARCHAR(255), type VARCHAR(255) CHECK(type in ('SSO', 'CLASSIC')) NOT NULL);
    CREATE TABLE IF NOT EXISTS devices (user_id VARCHAR(255), device_uuid VARCHAR(255), PRIMARY KEY (user_id, device_uuid), FOREIGN KEY(user_id) REFERENCES users(id));
    CREATE TABLE IF NOT EXISTS trust (a INTEGER, b INTEGER, PRIMARY KEY (a, b), FOREIGN KEY(a) REFERENCES users(id), FOREIGN KEY(b) REFERENCES users(id));
    `);
  };

  static drop = () => {
    db.instance.exec("DROP TABLE IF EXISTS users; DROP TABLE IF EXISTS trust;");
  };
}
