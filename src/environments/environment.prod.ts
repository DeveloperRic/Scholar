const clientDomain = 'https://eager-ritchie-3b02e8.netlify.app'

export const environment = {
  production: true,
  AUTH0_DOMAIN: 'scholar-app.us.auth0.com',
  AUTH0_CLIENT_ID: 'mtUXGbYYSMwB0dQTyL3z2aKVswND0bDu',
  AUTH0_REDIRECT_URI: `${clientDomain}/login/callback`,
  AUTH0_LOGOUT_URI: clientDomain,
  REALM_APP_ID: 'scholar-tbawx'
}
