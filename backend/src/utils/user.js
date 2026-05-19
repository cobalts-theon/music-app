const toAuthUser = (user) => ({
  id: user.id,
  email: user.email,
  displayName: user.display_name,
  avatarUrl: user.avatar_url,
  role: user.role || 'user'
});

const toProfileUser = (user) => ({
  id: user.id,
  email: user.email,
  displayName: user.display_name,
  avatarUrl: user.avatar_url,
  role: user.role || 'user',
  createdAt: user.created_at
});

module.exports = {
  toAuthUser,
  toProfileUser
};
