// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  AUTH0_DOMAIN: 'scholar-app.us.auth0.com',
  AUTH0_CLIENT_ID: 'mtUXGbYYSMwB0dQTyL3z2aKVswND0bDu',
  AUTH0_REDIRECT_URI: 'https://localhost:4200/login/callback',
  AUTH0_LOGOUT_URI: 'https://localhost:4200',
  REALM_APP_ID: 'scholar-tbawx'
}

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
