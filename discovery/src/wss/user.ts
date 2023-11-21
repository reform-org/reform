import { db } from "../utils/db.js"
import bcrypt from "bcrypt"
import { randomUUID } from "crypto"
import jwt from "jsonwebtoken"

export enum UserTypes {
    SSO = "SSO",
    Classic = "CLASSIC"
}


export abstract class User {
    name: string
    id: string
    displayId: string
    type: UserTypes

    public constructor();
    public constructor(id: string, name: string);
    public constructor(id?: string, name?: string) {
       this.apply(id, name)
    }

    protected toDisplayId(id: string) {
        return id
    }

    protected apply(id?: string, name?: string) {
        this.id = id ?? ""
        this.name = name ?? ""
        this.displayId = id ? this.toDisplayId(id) : ""
    }

    public async fromID(id: string) {
        const user = await db.get("SELECT * FROM users WHERE id = ?", id)
        if (user) {
            this.apply(user.id, user.name)
        }

        return this
    }

    public trust(user: User) {
        db.instance.run("INSERT OR REPLACE INTO trust(a, b) VALUES (?, ?)", this.id, user.id);
    }

    public withdrawTrust(user: User) {
        db.instance.run("DELETE FROM trust WHERE a = ? AND b = ?", this.id, user.id);
    }

    public storeName() {
        db.instance.run("UPDATE users SET name = ? WHERE id = ?", this.name, this.id)
    }

    public async getConnectableUsers(): Promise<Array<User>> {
        const users = (
            await db.all(`
                SELECT name, id, type, EXISTS(SELECT * FROM devices WHERE user_id = id) as online FROM users 
                WHERE EXISTS(SELECT * FROM trust WHERE (a = ? AND b = id)) 
                AND EXISTS(SELECT * FROM trust WHERE (a = id AND b = ?))`, this.id, this.id)
        ).filter(p => p.online).map(user => createUser(user.type, user.id, user.name));

        users.push(this)
        return users
    }

    public async getAvailableUsers(): Promise<Array<AvailableUser>> {
        const users = (
            await db.all(`
            SELECT name, id, type, EXISTS(SELECT * FROM devices WHERE user_id = id) as online,
            EXISTS(SELECT * FROM trust WHERE a = ? AND b = id) as trusted, 
            (EXISTS(SELECT * FROM trust WHERE a = ? AND b = id) AND EXISTS(SELECT * FROM trust WHERE a = id AND b = ?)) as mutualTrust 
            FROM users 
            WHERE NOT id = ?`, this.id, this.id, this.id, this.id)
        ).filter(p => p.online).map(user => new AvailableUser(user.type, user.id, user.name, user.trusted, user.mutualTrust));
        return users
    }

    public issueToken(): UserToken {
        const maxAgeDays = 14
        return {
            access_token: jwt.sign({ username: this.name, type: this.type, uuid: this.id, device: randomUUID() }, process.env.JWT_KEY, { expiresIn: `${maxAgeDays}d` }),
            maxAge: maxAgeDays * 86400000
        }
    }
}

interface UserToken {
    access_token: string
    maxAge: number
}

export class AvailableUser{
    trusted: boolean
    mutualTrust: boolean
    name: string
    id: string
    displayId: string
    type: UserTypes

    public constructor(type: UserTypes, id: string, name: string, trusted: boolean, mutualTrust: boolean) {
        this.trusted = trusted
        this.mutualTrust = mutualTrust
        this.id = id
        this.name = name
        this.type = type

        this.displayId = createUser(type, id, name).displayId
    }
}

export class ClassicUser extends User{
    password: string
    type: UserTypes = UserTypes.Classic

    public constructor();
    public constructor(id: string, name: string);
    public constructor(id?: string, name?: string) {
       super(id, name)
    }

    protected override toDisplayId(id: string) {
        return id.substring(0, 8)
    }

    public async fromName(name: string) {
        const user = await db.get("SELECT * FROM users WHERE name = ? AND type = 'CLASSIC'", name)
        if (user) {
            this.apply(user.id, user.name)
            this.password = user.password
        }

        return this
    }

    public isPasswordValid(password: string): boolean {
        return bcrypt.compareSync(password, this.password)
    }
}

export class SSOUser extends User{
    type: UserTypes = UserTypes.SSO

    public constructor();
    public constructor(tuid: string, name: string);
    public constructor(tuid?: string, name?: string) {
       super(tuid, name)
    }
}

export const createUser = (type: UserTypes, id?: string, name?: string): User => {
    if(type === UserTypes.Classic) return new ClassicUser(id, name)
    else return new SSOUser(id, name)
}