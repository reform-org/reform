import express from "express"
import { SentMessageInfo } from "nodemailer";
import { Attachment } from "nodemailer/lib/mailer/index.js";
import { error } from "../helpers.js";
import { Mail, Mailer } from "../mailer.js";
import { authenticateToken } from "../middleware.js";
import { serverPath } from "../server.js";

export const mailRouter = async () => {
    const router = express.Router()

    router.post(`${serverPath}/mail`, authenticateToken, async (req, res) => {
        const from = req.body?.from;
        if (!from) return res.status(400).json(error("from must not be empty!", ["from"]));
        const fromName = req.body?.fromName;
        if (!fromName) return res.status(400).json(error("fromName must not be empty!", ["fromName"]));
        const to = req.body?.to;
        if (!to) return res.status(400).json(error("to must not be empty!", ["to"]));
        const html = req.body?.html;
        if (!html) return res.status(400).json(error("html must not be empty!", ["html"]));
        const subject = req.body?.subject;
        if (!subject) return res.status(400).json(error("subject must not be empty!", ["subject"]));

        const options = {from: from, fromName: fromName, to: to, html: html, subject: subject}
        if(req.body?.cc) options["cc"] = req.body.cc
        if(req.body?.bcc) options["bcc"] = req.body.bcc

        if(!Mailer.isSetup()) return res.json({accepted: [], rejected: [to, options["cc"], options["bcc"]].flat().filter(a => a !== "")})

        const mailer = Mailer.getInstance()

        if(req.body?.attachments) options["attachments"] = req.body.attachments.map(a => {
            const attachment: Attachment = {
                content: Buffer.from(a.content),
                contentType: a.contentType,
                filename: a.filename
            }
            console.log(attachment, a)
            return attachment
        })

        const mail = new Mail(options)
        const answer: SentMessageInfo = await mailer.send(mail)

        res.json({accepted: answer.accepted, rejected: answer.rejected})
    })

    return router
}