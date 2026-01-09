# 🎬Video Subtitle Provider
<sub>**⚠️Windows desktop app!**</sub> \
<sub>**⚠️Not for commercial use!**</sub>

The app for adding automatically generated subtitles (.ass) to video files.\
The app can provide machine translation of the original text into the selected language. \
The current 'Language selection' can be expanded ([instruction.docx](presentation_materials/instraction.docx))!

## ✨Features

- Add subtitles to video files
- Supported formats: MP4, AVI, MOV, MKV, WMV
- Customize subtitle settings:
    - Language selection (Russian/English)
    - Font size (Small/Medium/Large)
    - Text and a background color
    - Transparency of the font (0% - 80%)
    - Subtitles screen position (Top/Center/Below)
  
## 📄Graphical user interface (GUI)
![GUI](presentation_materials/GUI.jpg)

## 🎥 Video subtitles demonstration:

<table align="center" style="border-collapse: collapse; border: none;">
  <tr>
    <td style="padding: 10px; text-align: center; vertical-align: top; border: none;">
      <img src="presentation_materials/rick_and_morty_en_demonstration.gif?1" 
           width="370" 
           style="display: block; margin: 0 auto; border: 1px solid #eee; border-radius: 5px;"
           alt="EN Demo"/>
      <div style="margin-top: 5px;"><strong>English</strong></div>
    </td>
    <td style="width: 20px; border: none;"></td> <!-- indent -->
    <td style="padding: 10px; text-align: center; vertical-align: top; border: none;">
      <img src="presentation_materials/rick_and_morty_ru_demonstration.gif?2" 
           width="370" 
           style="display: block; margin: 0 auto; border: 1px solid #eee; border-radius: 5px;"
           alt="RU Demo"/>
      <div style="margin-top: 5px;"><strong>Russian</strong></div>
    </td>
  </tr>
</table>

## 🔧Schema of modules interaction
![schema](presentation_materials/Inner_interaction_schema.png)

## 💻OS requirements for launching!

- Windows 10 or higher

### 🚀How to launch the app
1. download the archive with the app
2. unzip this where you need
3. launch **"JavaSubProviderApp.bat"** file inside the archive
- **Notice:** The first launch can take more time than subsequence next ones, because the app has to be compiled.

### ⚙️Stack of technology:

- Java 21
- Python 3.12.10
- Httpclient 4.5.13
- Lombok 1.18.34
- Json 20231013
- JavaFX-controls 21.0.1
- JavaFX-fxml 21.0.1
- Vosk 0.3.45
- Libretranslate 1.8.3
- FFMPEG 2025-12-07 (essential)