import sys
import yt_dlp
import subprocess
import os

url = sys.argv[1]
filename = sys.argv[2]

base_path = "C:/Users/HP/Downloads/demo1/demo1/uploads/"
output_template = base_path + filename.replace(".mp3", "")

ydl_opts = {
    'format': 'bestaudio/best',
    'outtmpl': output_template,
    'postprocessors': [{
        'key': 'FFmpegExtractAudio',
        'preferredcodec': 'mp3',
        'preferredquality': '192',  # ✅ STEP 3 (high quality audio)
    }],
}

with yt_dlp.YoutubeDL(ydl_opts) as ydl:
    ydl.download([url])

# ✅ Correct final file name
final_file = filename  # already .mp3

# 🔥 Run transcription
subprocess.run([
    "python",
    "C:/Users/HP/Downloads/demo1/demo1/transcribe.py",
    final_file
])