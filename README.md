# NotePod Android — MVP

Android port of the Windows NotePod (PyQt6) hierarchical note-taking app.

## How to open

1. Open **Android Studio** (Hedgehog 2023.1+ recommended)
2. `File → Open` → select this folder (`notepod-android/`)
3. Let Gradle sync finish
4. Run on emulator (API 26+) or a physical device

---

## MVP feature parity with Windows app

| Windows feature | Android MVP |
|---|---|
| Hierarchical tree (left panel) | RecyclerView with depth-indented rows |
| Expand / collapse nodes | ▸ / ▾ chevron button per row |
| Click node → edit | Tap row → EditorActivity |
| Add child node | Long-press row → dialog |
| Add sibling / root node | FAB / toolbar + button |
| Delete node + subtree | Swipe-left → confirm dialog |
| Persist data (.iectta format) | Room SQLite (same schema: id, title, content, parentId) |
| Auto-save | Saved on back-press / up-press in editor |
| Welcome seed data | Inserted on first DB creation |

---

## Progressive upgrade roadmap

1. **Complete remaining layouts** — `activity_editor.xml`, drawables, menus *(already included)*

2. **Rich text editor** — Replace plain `EditText` in `EditorActivity` with
   [`Markwon`](https://github.com/noties/Markwon) for Markdown rendering, or a
   `WebView`-based editor (Quill / ProseMirror) to match the Windows QTextEdit.

3. **Drag-and-drop reordering** — Enable `ItemTouchHelper` drag callbacks in
   `NoteTreeAdapter`; update `sortOrder` in Room on drop.

4. **Inline find/replace bar** — Add a `SearchView` + Replace `EditText` pinned
   above the soft keyboard in `EditorActivity` (mirrors Windows Ctrl+F bar).

5. **24-theme system** — Define `ColorScheme` objects for each theme name from
   the Windows app; persist chosen theme via `DataStore<Preferences>` and apply
   via `AppCompatDelegate` / Material3 dynamic color.

6. **Auto-backup** — `WorkManager` `PeriodicWorkRequest` (15 min minimum) that
   serialises the full tree to `files/backups/auto/<title>_auto_<timestamp>.json`
   — mirrors the Windows `.iectta` auto-backup logic.

7. **Import / Export** — `MultiImporterDialog` using `Storage Access Framework`
   (`ACTION_OPEN_DOCUMENT`) to read `.md`, `.opml`, `.json`, `.txt` and feed
   `NoteRepository.addNote()`. Export branch/tree to clipboard as indented text.

8. **Cloudflare Pages publish** — Settings screen (API token + project name) +
   OkHttp background call replicating `notepod_cf_multitenant.py`; progress shown
   in a `BottomSheet`.

9. **Plugin / macro engine** — Load JS scripts from `assets/plugins/` via
   Android's `JavascriptInterface` in a sandboxed `WebView`, or use the
   [QuickJS Android binding](https://github.com/nicowillis/quickjs-android) to
   mirror the Python plugin API.

10. **Sync / cloud** — Add a Firebase Firestore or Supabase backend so documents
    are available across devices; merge strategy based on `updatedAt` timestamp.
