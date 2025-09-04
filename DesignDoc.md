# 📖 Collections / Discoverables System
*Game Feature Design Document*

---

## Overview

**Collections** is a dynamic and modular system designed for RPG progression and discovery. At its core, it provides a **menu interface** where players can browse various types of discoverables and track their progress unlocking them.

This system is not exclusive to *Spellbrook* — it is designed to be **generic**, **server-admin-friendly**, and **API-extensible** for any Minecraft RPG or progression server.

---

## Plugin Design Philosophy

- **Modular and Generic**: The core system supports multiple collection types and is not hardcoded to any specific game content.
- **Configuration-Driven**: Collection types, entries, display logic, and unlock triggers are all defined in configuration files.
- **Server-Friendly APIs**: External plugins or datapacks can:
  - Register custom collection repositories via API or config.
  - Trigger discoveries using simple command-based hooks.
- **Portability**: Entire collection groups can be ported between servers with minimal changes (e.g., copy a folder, update namespace IDs).
- **Extensible Trigger System**: Discovery can be triggered by:
  - Right-clicking a furniture item
  - Picking up, crafting, or breaking specific items
  - Running a command such as:
    ```
    /collectables unlock <player> <repository>:<id>
    ```

---

## ✨ Feature Summary

- A unified `/collectables` menu where players view their progress across different collection types
- Collection types are modular and configurable
- Each collectable can be discovered through interaction with world content
- First implementation will focus on the **“Notes”** collection type

---

## Initial Scope: Notes

### What is a Note?

- A **Note** is a *written book item* (book and quill or signed book) containing **lore or story content**
- It is found within the world and revealed through player discovery
- Visually represented in-world using **Nexo furniture items** (e.g. placed books, scrolls, desks)
- We won't actually deal with the in game item, we'll make a debug dummy collectable interaction for the MVP

### Discovery Mechanism

- Upon **first interaction** (e.g., right-click), the player:
  - Unlocks the Note in their Collections menu
  - Is shown the lore/story via a written book interface
  - Has configured commands executed (e.g., sounds, messages, XP reward)

### Technical Implementation (Notes)

- Notes are placed using **Nexo Furniture Items**
- Two possible discovery methods:
  1. **Command-bound Furniture**:
    - Trigger a discovery command via interaction
    - Easier to configure without needing API

- Discovery data is stored **per-player** to track what they've unlocked
- Configurable book content (title, author, pages) per Note

---

## 🧩 Planned Expansion (Future Types)

The system is built with extensibility in mind. Example future collection types:

| Type           | Description                                    |
|----------------|------------------------------------------------|
| Recipes        | Custom crafting recipes discovered in the world. |
| Fish           | Caught or rare fish variants.                 |
| Music          | Soundtracks or musical artifacts.             |
| Easter Eggs    | Hidden references or secret spots.            |
| Holiday Gifts  | Seasonal items found during events.           |
| Gatherables    | Plants, minerals, or other world resources.   |
| Runes          | Magical symbols found and studied.            |

Each type can have:

- Its own icon, display category, and progression tracker
- Configurable discovery logic
- Option to hide/show undiscovered items

---

## ⚙️ Configuration System

- All collectable repositories (e.g., `notes`, `recipes`, `fish`) are defined via config files
- Each entry is uniquely identified using a namespaced ID format:
  ```
  notes:forgotten_path
  runes:fire_lens
  music:overworld_theme
  ```

- Server admins or datapacks can create their own custom repositories by adding new folders/configs - no coding required

### Psuedo/Draft Example Note Entry

```yaml
notes:
mysterious_note:
display_name: "Mysterious Note"
icon: BOOK
book:
title: "The Forgotten Path"
author: "Unknown"
pages:
  - "The path winds where eyes can’t see..."
  - "It waits for one who dares follow."
commands_on_discover:
  - "msg <player> &7You feel a strange connection to this place."
  - "playsound minecraft:entity.illusioner.cast_spell master <player>"
show_when_locked: true
```

### Global Options

```yaml
settings:
hide_locked_entries: false
allow_replay: true
menu_title: "<gold>Collections</gold>"
```

---

## 🛠Rough Developer Tasks

- [ ] Implement `CollectableManager` to manage available entries and their unlock logic.
- [ ] Implement repository auto-discovery from config directory.
- [ ] Create `NoteDiscoverable` implementation and registry system.
- [ ] Integrate with `/collectables` GUI menu.
- [ ] Add generic `/collectables unlock <player> <type>:<id>` command.
- [ ] Provide optional auto-hooks (e.g., on pickup/break) for common use cases.
- [ ] Implement player unlock storage and retrieval.
- [ ] Design GUI layout system (with support for multiple types).
- [ ] Implement written book presentation logic.
- [ ] Provide basic Java API and events for other plugins to integrate.

---

## Test Cases

- Player discovers a Note → menu updates → book opens
- Discoverable rediscovered → book reopens (if replay enabled)
- Commands execute correctly on first-time unlock
- Hidden vs visible locked entries behave per config
- New repositories are picked up automatically
- Command-based unlocks work with placeholder resolution
- Storage persists correctly across restarts

---

## Final Notes

This system is designed as a **plugin platform**, not just one server-specific mechanic.   
It will support a wide range of gameplay types - from RPGs to survival-enhanced exploration servers

From books in ancient ruins to runes embedded in stone - server owners can create meaningful lore,  
content, and rewards with minimal setup and rich immersion.

---