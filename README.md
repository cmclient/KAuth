# KAuth - Authorization Plugin for Minecraft

KAuth is an advanced authorization plugin for Minecraft, offering secure account management and user authentication.

## Features

- **User Registration and Login**: Easy-to-use commands for player authentication.
- **Password Management**: Secure password change functionality.
- **Session Handling**: Automatic login sessions for convenience.
- **Visual Feedback**: Experience bar integration.
- **Customization**: Additional features like the pumpkin effect.

## Supported Minecraft Versions

KAuth supports Minecraft **1.8 - 1.26.3**.

## Requirements

- [TitleAPI](https://github.com/spacehost-me/titleapi) is **required** and must be installed on the server.

  If you prefer not to install it separately, grab the release jar with TitleAPI bundled
  (`KAuth-<version>-with-dependencies.jar`) from the
  [releases page](https://github.com/cmclient/KAuth/releases) instead of the plain `KAuth-<version>.jar`.

## Download

Get the latest build from the [releases page](https://github.com/cmclient/KAuth/releases):

- `KAuth-<version>.jar` - requires TitleAPI to be installed separately.
- `KAuth-<version>-with-dependencies.jar` - TitleAPI bundled.

## Commands

- `/login` (aliases: `/log`, `/l`): Log in to your account.
- `/register` (aliases: `/reg`, `/r`): Register a new account.
- `/changepassword` (alias: `/changepass`): Change your account password.
- `/logout` (alias: `/logoff`): Log out from your account.
- `/auth`: Admin command - manage other users.

## Configuration

Customize KAuth via the `config.yml` file to set up database connections, messages, features, and user restrictions. Ensure to provide correct MySQL credentials and adjust message templates to fit your server's needs.

### Database

> **Note:** SQLite is currently broken - only **MySQL** is working. Set `database.type` to `mysql` in `config.yml`.
