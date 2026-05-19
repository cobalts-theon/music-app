const fs = require('fs');
const path = require('path');
const nodemailer = require('nodemailer');
const { AppError } = require('../middleware/errorHandler');

const logoPath = path.join(__dirname, '..', 'assets', 'CindersoulsWhite.png');
let transporter;

const readMailEnv = (...keys) => {
  for (const key of keys) {
    const value = process.env[key];
    if (value && value.trim()) {
      return value.trim();
    }
  }
  return '';
};

const getMailConfig = () => {
  const rawPort = readMailEnv('MAIL_PORT', 'SMTP_PORT');
  const parsedPort = Number(rawPort || 587);
  const port = Number.isFinite(parsedPort) ? parsedPort : 587;
  const secureSetting = readMailEnv('MAIL_SECURE', 'SMTP_SECURE');

  return {
    host: readMailEnv('MAIL_HOST', 'SMTP_HOST'),
    port,
    secure: secureSetting ? secureSetting.toLowerCase() === 'true' : port === 465,
    user: readMailEnv('MAIL_USER', 'SMTP_USER'),
    pass: readMailEnv('MAIL_PASS', 'SMTP_PASS'),
    from: readMailEnv('MAIL_FROM', 'SMTP_FROM')
  };
};

const getMissingMailConfigKeys = () => {
  const config = getMailConfig();
  const missing = [];

  if (!config.host) missing.push('MAIL_HOST/SMTP_HOST');
  if (!config.user) missing.push('MAIL_USER/SMTP_USER');
  if (!config.pass) missing.push('MAIL_PASS/SMTP_PASS');

  return missing;
};

const isMailConfigured = () => {
  return getMissingMailConfigKeys().length === 0;
};

const getTransporter = () => {
  if (!isMailConfigured()) return null;
  if (transporter) return transporter;
  const config = getMailConfig();

  transporter = nodemailer.createTransport({
    host: config.host,
    port: config.port,
    secure: config.secure,
    auth: {
      user: config.user,
      pass: config.pass
    }
  });

  return transporter;
};

const getFromAddress = () => {
  const config = getMailConfig();
  return config.from || `"Cinder's Soul" <${config.user}>`;
};

const escapeHtml = (value = '') => {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
};

const baseTemplate = ({ preview, title, body, actionBlock = '' }) => {
  return `
    <!doctype html>
    <html>
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>${escapeHtml(title)}</title>
      </head>
      <body style="margin:0;background:#08090d;color:#f3eee7;font-family:Arial,Helvetica,sans-serif;">
        <div style="display:none;max-height:0;overflow:hidden;opacity:0;">${escapeHtml(preview)}</div>
        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background:#08090d;">
          <tr>
            <td align="center" style="padding:28px 16px;">
              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#11151c;border:1px solid rgba(255,255,255,0.10);border-radius:14px;overflow:hidden;">
                <tr>
                  <td align="right" style="background:#050608;padding:18px 22px;text-align:right;">
                    <img src="cid:cinders-logo" alt="Cinder's Soul" width="118" style="display:inline-block;border:0;outline:none;text-decoration:none;">
                  </td>
                </tr>
                <tr>
                  <td style="padding:30px 28px 34px;">
                    <h1 style="margin:0 0 12px;font-size:26px;line-height:1.2;color:#ffffff;">${escapeHtml(title)}</h1>
                    <div style="font-size:15px;line-height:1.65;color:#d6d9dd;">${body}</div>
                    ${actionBlock}
                    <p style="margin:30px 0 0;font-size:12px;line-height:1.5;color:#8f98a3;">Cinder's Soul</p>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </body>
    </html>
  `;
};

const sendMail = async ({ to, subject, html, text, requireConfigured = false }) => {
  const mailer = getTransporter();
  if (!mailer) {
    if (requireConfigured || process.env.NODE_ENV === 'production') {
      const missingKeys = getMissingMailConfigKeys();
      const detail = missingKeys.length ? ` Missing: ${missingKeys.join(', ')}.` : '';
      throw new AppError(`Email service is not configured.${detail}`, 500);
    }

    console.warn(`[mail] SMTP is not configured. Skipped email to ${to}: ${subject}`);
    return false;
  }

  const mailPayload = {
    from: getFromAddress(),
    to,
    subject,
    text,
    html
  };

  if (fs.existsSync(logoPath)) {
    mailPayload.attachments = [
      {
        filename: 'cinders-soul-logo.png',
        path: logoPath,
        cid: 'cinders-logo'
      }
    ];
  }

  await mailer.sendMail(mailPayload);

  return true;
};

const sendWelcomeEmail = async ({ to, displayName }) => {
  const safeName = escapeHtml(displayName || 'listener');
  const html = baseTemplate({
    preview: "Welcome to Cinder's Soul.",
    title: "Welcome to Cinder's Soul",
    body: `
      <p style="margin:0 0 12px;">Hi ${safeName},</p>
      <p style="margin:0;">Your account is ready. Start building your library, saving favorites, and creating playlists.</p>
    `
  });

  return sendMail({
    to,
    subject: "Welcome to Cinder's Soul",
    text: `Hi ${displayName || 'listener'}, your Cinder's Soul account is ready.`,
    html
  });
};

const sendPasswordResetOtpEmail = async ({ to, displayName, otp, expiresInMinutes }) => {
  const safeName = escapeHtml(displayName || 'listener');
  const safeOtp = escapeHtml(otp);
  const html = baseTemplate({
    preview: `Your Cinder's Soul OTP is ${otp}.`,
    title: 'Password reset OTP',
    body: `
      <p style="margin:0 0 12px;">Hi ${safeName},</p>
      <p style="margin:0 0 18px;">Use this OTP to reset your password. It expires in ${Number(expiresInMinutes)} minutes.</p>
    `,
    actionBlock: `
      <div style="margin-top:18px;padding:16px 18px;background:#08090d;border:1px solid rgba(225,74,59,0.55);border-radius:10px;text-align:center;">
        <span style="font-size:30px;letter-spacing:7px;font-weight:700;color:#ffffff;">${safeOtp}</span>
      </div>
      <p style="margin:18px 0 0;font-size:13px;line-height:1.5;color:#aeb5bd;">If you did not request this, you can ignore this email.</p>
    `
  });

  return sendMail({
    to,
    subject: "Your Cinder's Soul OTP",
    text: `Your Cinder's Soul OTP is ${otp}. It expires in ${expiresInMinutes} minutes.`,
    html,
    requireConfigured: true
  });
};

module.exports = {
  isMailConfigured,
  sendWelcomeEmail,
  sendPasswordResetOtpEmail
};
