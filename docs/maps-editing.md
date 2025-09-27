---
layout: default
title: Map Editing
permalink: /maps-editing/
---

<p align="center">
  <a href="/">Home</a> •
  <a href="/privacy/">Privacy</a> •
  <a href="/install/">Install</a> •
  <a href="/maps/">Maps</a> •
  <a href="/maps-editing/">Map Editing</a> •
  <a href="/contributing/">Contribute</a> •
  <a href="/faq/">FAQ</a> •
</p>

# Editing PMZ Maps for aMetro

aMetro uses the **PMZ map format**, originally from **pMetro**.  
PMZ files are just ZIP archives with configuration and graphics, so they’re editable with the classic **pMetro Editor**.

---

## 📥 Download the Editor

- **Editor package:** [pMetro Editor (zip)](http://pmetro.su/download/pMetroEditor.zip)  
- Includes: `pMetroEditor.exe`, helper tools (e.g. `pVecEditor.exe`), minimal configs.

### Where to get `pMetro.ini`
Some editor bundles may not include a fresh `pMetro.ini`. Install **pMetro** for Windows and copy it from there:
- **Windows installer:** [pMetro Setup](http://pmetro.su/download/pMetroSetup.exe)  
- After installing, you’ll find `pMetro.ini` in the pMetro installation folder.

---

## 🔧 Installation

1. Create a new folder.  
2. Copy into it:  
   - `pMetroEditor.exe`  
   - `pMetro.ini` (from the editor zip or the pMetro installation)  
   - A `.pmz` file you want to edit.  
3. Rename the `.pmz` file to `.zip` and extract it.  
4. Inside you’ll find `metro.ini` plus related files (`.map`, `.trp`, images, etc.).  
5. Launch `pMetroEditor.exe` to make graphical adjustments, or edit the INI/text files manually.  
6. Repack the folder into a ZIP and rename it back to `.pmz`.  
7. Place the edited `.pmz` back into aMetro’s maps directory for testing.

---

## 🔎 Finding existing maps to patch

If you want to **patch an existing city map**, install pMetro and look inside its installation directory — you’ll find the shipped map files there (as `.pmz`).  
- Copy the relevant `.pmz` out, rename to `.zip`, and follow the editing steps above.  
- When finished, zip it back and rename to `.pmz`.

---

## 🖼️ File Structure Highlights

- **`metro.ini`** — main map metadata (city names, authors, version requirements).  
- **`*.trp`** — line definitions (stations, aliases, timings, transfers).  
- **`*.map`** — visual layout, colours, coordinates.  
- **Images** — background graphics (BMP, GIF, PNG, or VEC).  
- **`metro.cty`** — city-level settings (names, localisation, delays).  
- **Optional `.txt` files** — extra station information.

---

## 🖱️ Editor Usage Tips

- **Move objects**: left-click and drag.  
- **Resize areas**: right-click and drag.  
- **Clipboard coordinates**: enable in menu to copy cursor positions.  
- **Vector editor (`pVecEditor.exe`)**:  
  - Left-click: move points.  
  - Right-click: zoom.  
  - **Shift + left-click**: select multiple points; to drag a group, hold **Shift**, start dragging on empty space with left mouse held, then release **Shift** mid-drag and continue moving the mouse.  
  - Supports polylines, splines, polygons, text labels, arrows, ellipses, transparency with `Opaque`, etc.

---

## 🧩 Travel Time & Transfer Data

- Travel times (`Driving=`) support precise values, e.g. `1.45` for 1m45s.  
- Transfers (`[Transfers]`) may be asymmetric, hidden (`invisible`), or flagged cross-platform in `[AdditionalInfo]`.  
- Delays (`Delays=`) and depths (`Heights=`) set waiting times and station depths.

---

## 🚉 Vector & 3D Formats

- **Vec format**: script-based vector drawing (`Line`, `Spline`, `Polygon`, `Ellipse`, etc.).  
- **3D layouts**: `pm3d` binary format is closed; only ASE → pm3d conversion via private tools is supported.

---

## 📨 Submitting your map

When you’ve created or corrected a map:

- **Email it to Boris (pMetro author):** **Boris@pMetro.su**  
  - Please mention “pMetro” in the subject line.  
- Optionally, also open a **GitHub Issue** in the aMetro repo describing the change and attach the `.pmz`, so it can be reviewed and (if accepted) bundled with aMetro.

---

## 📬 Contact

- pMetro site: <http://pMetro.su>  
- aMetro issues: <https://github.com/RomanGolovanov/ametro/issues>
