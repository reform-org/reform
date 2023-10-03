import dotenv from "dotenv"
import nodemailer, { Transporter } from "nodemailer"
import { Attachment } from "nodemailer/lib/mailer"

dotenv.config()

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
        return process.env.SMTP_USERNAME !== "" 
        && process.env.SMTP_PASSWORD !== "" 
        && process.env.SMTP_HOST !== "" 
        && process.env.SMTP_PORT !== "" 
        && process.env.SMTP_SENDER !== ""
    }

    static getInstance() {
        if (!Mailer.instance) {
            Mailer.instance = new Mailer();
        }
        return Mailer.instance;
    }

    public async createConnection() {
        this.transporter = nodemailer.createTransport({
            host: process.env.SMTP_HOST,
            port: parseInt(process.env.SMTP_PORT),
            secure: parseInt(process.env.SMTP_PORT) === 465,
            auth: {
                user: process.env.SMTP_USERNAME,
                pass: process.env.SMTP_PASSWORD,
            },
        });
    }

    public async send(mail: Mail) {
        return await this.transporter
            .sendMail({
                from: `${mail.options.fromName || ""} ${process.env.SMTP_SENDER || mail.options.from}`,
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
