# ESP32 Hardware Guide

Kado supports syncing flashcard decks to an ESP32-based hardware device for offline review. The device runs a WiFi access point that the app connects to, allowing you to upload decks for study without a phone.

The firmware for the Kado ESP32 module is kept in a private repository, but the communication protocol and deck format are fully documented here so anyone can build a compatible device.

## How It Works

1. The ESP32 device creates a WiFi access point with SSID prefix `KadoLite-`.
2. The Kado app connects to the device's WiFi network.
3. Decks are converted to the `.ald` format (plain text, no HTML/images) and uploaded via HTTP.
4. The device stores decks locally for offline review on its display.

## Connecting to the Device

1. Power on the ESP32 device.
2. In Kado, open a deck and tap **Transfer to Device**.
3. The app attempts to connect to the device's WiFi network (`KadoLite-*`).
4. Once connected, the app communicates with the device at `192.168.4.1`.

### Platform-Specific WiFi Handling

- **Android**: Uses `WifiNetworkSpecifier` to connect to the device network without disrupting the user's primary WiFi.
- **iOS**: Uses `NEHotspotConfiguration` for targeted WiFi connection to the device.

## Communication Protocol

The device runs an HTTP server at `192.168.4.1` with the following endpoints:

### Endpoints

| Endpoint | Method | Description |
|---|---|---|
| `/api/decks` | GET | List decks stored on the device |
| `/upload` | POST | Upload a deck file (`.ald` format) |
| `/api/delete?idx=<index>` | DELETE | Delete a deck by its index |

### GET /api/decks

Returns a JSON object listing all decks on the device and available storage.

```json
{
  "decks": [
    { "name": "Spanish Vocab", "cards": 150 },
    { "name": "Biology", "cards": 80 }
  ],
  "free_mb": 2.4
}
```

### POST /upload

Upload a deck file as multipart form data.

- **Content-Type**: `multipart/form-data`
- **Field name**: `file`
- **File format**: `.ald` (see format specification below)
- **Response**: HTTP 2xx on success

### DELETE /api/delete?idx=\<index\>

Delete a deck by its 0-based index.

- **Query parameter**: `idx` — the index of the deck to delete (as returned by `/api/decks`)
- **Response**: HTTP 2xx on success

## .ald Deck Format

The `.ald` (Anki-Like Deck) format is a simple text-based format for transferring flashcard data to the ESP32 device.

### Format Specification

```
ALD1
<deck_name>
<card_count>
<front_1>\t<back_1>
<front_2>\t<back_2>
...
```

| Line | Content |
|------|---------|
| 1 | Format identifier: `ALD1` |
| 2 | Deck name (UTF-8 string) |
| 3 | Number of cards (integer) |
| 4+ | One card per line — front and back separated by a tab character (`\t`) |

### Example

```
ALD1
Spanish Vocab
3
hello	hola
goodbye	adiós
thank you	gracias
```

### Encoding Rules

- **Encoding**: UTF-8
- **Newlines** within card text are escaped as `\n`
- **Tabs** within card text are replaced with spaces
- **HTML tags** are stripped (converted to plain text). Block-level tags (`<br>`, `<div>`, `<p>`, `<li>`, `<blockquote>`, `<h1>`–`<h6>`) are converted to newlines before stripping.
- **Image markers** (`[img:...]`) are removed
- **HTML entities** are decoded (`&amp;` → `&`, `&nbsp;` → space, `&lt;` → `<`, `&gt;` → `>`, `&quot;` → `"`, `&#39;` → `'`)
- **Blank cards** (empty front or back after stripping) are excluded

### Filename Convention

Deck filenames are generated automatically:

1. Lowercase the deck name
2. Replace non-alphanumeric characters with underscores
3. Collapse consecutive underscores
4. Trim leading/trailing underscores
5. Append `.ald`

Example: `"Spanish Vocab 101"` → `spanish_vocab_101.ald`

## Building Your Own Device

Any ESP32 with WiFi capability and a display can implement this protocol. Key requirements:

- Run a WiFi access point with SSID prefix `KadoLite-`
- Serve an HTTP server on `192.168.4.1`
- Implement the three endpoints above (`/api/decks`, `/upload`, `/api/delete`)
- Parse the `.ald` format to extract cards
- Only plain text content is transferred — rich text, HTML, and images are stripped during conversion
