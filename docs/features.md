# Feature Reference

Detailed documentation for all Kado features. For a quick-start tutorial, see [Getting Started](getting-started.md).

## Spaced Repetition Algorithms

Kado supports two scheduling algorithms. Each deck can use a different algorithm, and you can switch at any time from the deck edit screen.

### SM2

The default algorithm, based on the classic SuperMemo SM2 system.

- Cards progress through three states: **New** → **Learning** → **Review**
- An **ease factor** (1.3 – 4.0) controls how quickly intervals grow
- Each rating adjusts the ease factor: Again decreases it, Easy increases it
- Intervals are clamped between 1 and 36,500 days

SM2 is straightforward and well-tested — a good default for most users.

### FSRS (Free Spaced Repetition Scheduler)

A modern algorithm based on the [open-source FSRS project](https://github.com/open-spaced-repetition/java-fsrs). It uses **stability** and **difficulty** as core parameters for more accurate retention modeling.

Cards move through three states: **Learning** → **Review** → **Relearning**.

FSRS is configurable per deck with the following parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| **Desired Retention** | 0.9 (90%) | Target probability of recalling a card at review time |
| **Learning Steps** | 1m, 10m | Intervals for cards in the learning state |
| **Relearning Steps** | 10m | Intervals for cards that lapsed back to relearning |
| **Maximum Interval** | 36,500 days | Upper bound for review intervals |
| **Enable Fuzzing** | Off | Adds randomness to intervals to avoid clustering reviews on the same day |

You can tune these parameters from the **Algorithm Detail** screen accessible in deck settings.

### Choosing Between SM2 and FSRS

- **SM2**: Simpler, fewer parameters, well-established. Good if you prefer a set-and-forget approach.
- **FSRS**: More modern and evidence-based. Better if you want fine-grained control over retention targets and scheduling behavior.

Both algorithms use the same four-button rating system (Again / Hard / Good / Easy).

## Deck Management

### Creating Decks

From the home screen, tap **+** to create a new deck. Configure the deck name, daily new card limit, and scheduling algorithm.

### Partitioning

Split a large deck into smaller sub-decks for focused study.

- **Batch mode** — Automatically partition by a fixed number of cards per sub-deck.
- **Manual mode** — Create custom sub-deck groupings.

Sub-decks are reviewed independently, each with their own statistics (card count, due count, new count).

### Cloning and Reversing

- **Clone** — Duplicate a sub-deck as a new independent deck.
- **Reverse** — Create a copy of a deck with front and back swapped. Useful for bidirectional study (e.g., learning vocabulary in both directions).

### Deleting

Decks can be deleted along with all their cards, review states, and media.

## Card Types

### Plain Text

Simple text on front and back. Auto-detected when card content contains no HTML or image markers.

### Rich Text / HTML

Cards containing HTML tags are automatically rendered with formatting. Supported tags:

| Category | Tags |
|----------|------|
| **Text style** | `<b>`, `<strong>`, `<i>`, `<em>`, `<u>` |
| **Headings** | `<h1>` through `<h6>` (with proportional font sizing) |
| **Structure** | `<div>`, `<p>`, `<br>`, `<hr>`, `<blockquote>` |
| **Lists** | `<li>`, `<ul>`, `<ol>` (rendered with bullet points) |
| **Color** | `<span style="color: ...">` with hex (`#ff0000`), `rgb()`, or named colors |

HTML entities (`&amp;`, `&nbsp;`, `&#39;`, etc.) are decoded automatically.

### Image Cards

Images can be embedded in cards and appear inline with text. During Anki import, `<img>` tags are converted to `[img:filename]` markers and the referenced media files are extracted and stored locally.

## Bulk Editing

Apply find-and-replace operations across all cards in a deck.

- **Separate patterns** for front and back content
- **Plain text or regex** matching (toggle per pattern)
- **Preview** changes before applying
- **Saved rules** — Name and persist find/replace patterns for reuse across sessions

Access bulk editing from the deck detail screen menu.

## Statistics and Analytics

Each deck has a statistics screen showing:

- **Card state breakdown** — New, learning, young (interval < 21 days), and mature (interval >= 21 days) cards
- **Due cards** — Number of cards due for review right now
- **Review progress** — Track your retention over time

You can reset all review progress for a deck from the statistics screen.

## Anki Import

Kado imports Anki `.apkg` files with full support for card templates and media.

### Supported Formats

- `collection.anki2` — Legacy Anki format
- `collection.anki21` — Modern Anki format

### Template Rendering

Anki's Mustache-like template syntax is fully supported:

| Syntax | Meaning |
|--------|---------|
| `{{FieldName}}` | Substitute the field's content |
| `{{#FieldName}}...{{/FieldName}}` | Show content only if the field is non-empty |
| `{{^FieldName}}...{{/FieldName}}` | Show content only if the field is empty |
| `{{FrontSide}}` | Embed the front template in the back card |
| `{{type:FieldName}}` | Type-in field (rendered as text) |
| `{{hint:FieldName}}` | Hint field |

### Media Extraction

Images referenced in Anki cards are extracted from the `.apkg` archive and stored in a per-deck media directory. `<img src="...">` tags are converted to `[img:filename]` markers for rendering.

## Localization

Kado is available in **English** (default) and **Spanish**. The language can be changed in Settings. All UI strings, labels, and messages are localized.

## Customization

| Setting | Description |
|---------|-------------|
| **Theme** | System, Light, or Dark mode |
| **Card Font Scale** | Adjust flashcard text size independently of the app UI |
| **App Font Scale** | Adjust text size throughout the entire app |
