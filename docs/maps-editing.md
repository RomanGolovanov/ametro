---
layout: default
title: Map Editing
permalink: /maps-editing/
---

<p align="center">
  <a href="/ametro">Home</a> •
  <a href="/ametro/privacy/">Privacy</a> •
  <a href="/ametro/install/">Install</a> •
  <a href="/ametro/maps/">Maps</a> •
  <a href="/ametro/maps-editing/">Map Editing</a> •
  <a href="/ametro/contributing/">Contribute</a> •
  <a href="/ametro/faq/">FAQ</a> •
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
- **`metro.cty`** — city-level settings (names, localisation, delays).  
- **`*.trp`** — line definitions (stations, aliases, timings, transfers).  
- **`*.map`** — visual layout, colours, coordinates.  
- **Images** — background graphics (BMP, GIF, PNG, or VEC).  
- **Optional `.txt` files** — extra station information.
---

## 🖼️ File Types in a PMZ Archive

Every PMZ archive is a collection of different file types. Each type plays a role in how the map is displayed, localized, and simulated inside aMetro.  

### metro.cty  
A city-level configuration file.  
How to fill:  
- Name: short name of the city.  
- CityName: same as above, or localized form.  
- Country: country name.  
- RusName: Russian version of the city name.  
- NeedVersion: minimal version required.  
- MapAuthors: free text, can include multiple lines.  
Optionally include localization, default delays, or language-specific information.  

Example:  
```
[Options]  
Name=Berlin  
CityName=Berlin  
Country=Germany  
RusName=Берлин  
NeedVersion=1.26.4  
MapAuthors=Scheme from 2008, based on UrbanRail.net  
```

### *.trp (line definitions)  
Each .trp file describes a type of network, such as U-bahn or S-bahn.  
Structure:  
- Options section at top, with Type=U-bahn or Type=S-bahn.  
- Then [LineX] sections for each line.  
- Inside a line section:  
  - Name: the line name (for example U1 or Ring).  
  - Stations: comma-separated list of stations, in travel order.  
  - Driving: list of travel times between stations. Must have one fewer number than the number of stations. Values can be integers for minutes or decimal values like 1.45 for 1 minute 45 seconds. Parentheses can be used for branches.  
  - DelayDay: typical waiting time at stations during the day.  
  - DelayNight: waiting time at night.  
  - Aliases: optional, list of alternative station names to unify duplicates.  

Example:  
```
[Options]  
Type=U-bahn  

[Line1]  
Name=U1  
Stations=Station A,Station B,Station C,Station D  
Driving=2,3,2  
DelayDay=4  
DelayNight=12  

[Line2]  
Name=U2  
Stations=North End,Midtown,South End  
Driving=5,6  
DelayDay=5  
DelayNight=15  
```

### *.map (graphical layout)  
A coordinate-based description of map graphics, typically used when bitmap backgrounds are present.  
How to fill:  
- Define station positions with coordinates.  
- Assign line colours.  
- Connect stations with line segments.  
- Reference optional image files for background.  
This file is usually produced with the pMetro Editor rather than by hand, but manual editing is possible if you know the syntax.  

### *.vec (vector schematic)  
A script-style file for vector drawing.  
How to fill:  
- size: define the canvas size in pixels, for example size 2000x1500.  
- PenColor: set the current pen colour, in hex like 00FF00.  
- TextOut: write text labels. Parameters include font, size, coordinates, text, and style flag.  
- Line: draw straight lines between coordinates.  
- Spline: draw curved connections.  
- Polygon and Ellipse: draw shapes.  
- Opaque: toggle transparency on or off.  

Example:  
```
size 1200x800  
PenColor FF0000  
TextOut Arial,20,100,200, U1,1  
Line 100,200,300,200  
```

### Images (bmp, gif, png)  
Bitmap backgrounds. Place the file in the archive and reference it in metro.ini or the .map file.  
How to fill:  
- Save the image in one of the supported formats.  
- Keep resolution close to the intended map canvas size.  
- Use clear lines and contrasting colours so stations and labels remain visible.  

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
