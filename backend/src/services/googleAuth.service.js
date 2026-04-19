const { OAuth2Client } = require('google-auth-library');
const { AppError } = require('../middleware/errorHandler');

const client = new OAuth2Client();

const getGoogleAudience = () => {
  const clientId = process.env.GOOGLE_CLIENT_ID;

  if (!clientId) {
    throw new AppError('Google auth is not configured. Missing GOOGLE_CLIENT_ID.', 500);
  }

  const audiences = clientId
    .split(',')
    .map((id) => id.trim())
    .filter(Boolean);

  if (!audiences.length) {
    throw new AppError('Google auth is not configured. Invalid GOOGLE_CLIENT_ID.', 500);
  }

  return audiences;
};

const verifyGoogleIdToken = async (idToken) => {
  let payload;

  try {
    const ticket = await client.verifyIdToken({
      idToken,
      audience: getGoogleAudience()
    });
    payload = ticket.getPayload();
  } catch (error) {
    throw new AppError('Invalid Google token', 401);
  }

  if (!payload || !payload.email) {
    throw new AppError('Google account has no email', 400);
  }

  if (!payload.email_verified) {
    throw new AppError('Google email is not verified', 401);
  }

  return {
    email: payload.email.toLowerCase(),
    displayName: (payload.name || payload.email.split('@')[0]).trim(),
    avatarUrl: payload.picture || null
  };
};

module.exports = {
  verifyGoogleIdToken
};
