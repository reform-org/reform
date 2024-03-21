import nodemailer from "nodemailer";
import { Attachment } from "nodemailer/lib/mailer";
import * as Globals from "../utils/globals.js";

export interface MailOptions {
    from: string
    fromName?: string
    to: string[] | string
    cc?: string[] | string
    bcc?: string[] | string
    subject: string
    html: string,
    attachments?: Attachment[]
}

export class Mail {
    options: MailOptions

    public constructor(options: MailOptions) {
        this.options = options
    }

    public getHtml() {
        let html = this.options.html || ""

        if (html.indexOf("<html>") < 0 && html.indexOf("</html>") < 0) html = `<html>\n${html}</html>\n`;
        if (html.indexOf("!doctype") < 0) html = `<!doctype html>\n${html}`;
        return html.replace(/ {4}| {2}|[\t\n\r]/gm, "")
    }

    public getPlainText() {
        let html = this.options.html || ""
        return html.split("\n")
            .map((line) => {
                if (line.replace(/<[^>]+>/g, "").match(/.*\S.*/)) {
                    return line.replace(/<[^>]+>/g, "").trim() + "\n";
                } else {
                    return (line = "");
                }
            })
            .join("")
            .trim();
    }
}

export class Mailer {
    private static instance: Mailer;
    private transporter: nodemailer.Transporter;

    public constructor() { }

    static isSetup() {
        return Globals.SMTP_USERNAME !== ""
        && Globals.SMTP_PASSWORD !== ""
        && Globals.SMTP_HOST !== ""
        && Globals.SMTP_PORT !== ""
        && Globals.SMTP_SENDER !== ""
    }

    static getInstance() {
        if (!Mailer.instance) {
            Mailer.instance = new Mailer();
        }
        return Mailer.instance;
    }

    public async createConnection() {
        this.transporter = nodemailer.createTransport({
            host: Globals.SMTP_HOST,
            port: parseInt(Globals.SMTP_PORT),
            secure: parseInt(Globals.SMTP_PORT) === 465,
            auth: {
                user: Globals.SMTP_USERNAME,
                pass: Globals.SMTP_PASSWORD,
            },
        });
    }

    public async send(mail: Mail) {
        return await this.transporter
            .sendMail({
                from: `${mail.options.fromName || ""} ${Globals.SMTP_SENDER || mail.options.from}`,
                to: mail.options.to,
                cc: mail.options.cc || "",
                bcc: mail.options.bcc || "",
                replyTo: `${mail.options.fromName || ""} ${mail.options.from}`,
                subject: mail.options.subject,
                text: mail.getPlainText(),
                html: mail.getHtml(),
                attachments: mail.options.attachments || []
            })
            .then((info) => {
                console.log(`Mail sent successfully!`);
                console.log(info)
                return info;
            });
    }
}
