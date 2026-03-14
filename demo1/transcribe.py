import whisper
import subprocess
import os
from transformers import pipeline
video_folder = "uploads"

output_path = os.path.join(video_folder, "output.txt")
summary_path = os.path.join(video_folder, "summary.txt")

if os.path.exists(output_path):
    os.remove(output_path)

if os.path.exists(summary_path):
    os.remove(summary_path)

model = whisper.load_model("base")

summarizer = pipeline("summarization")

video_folder = "uploads"

allowed_extensions = (".mp4",".mkv",".mov",".avi",".mp3",".wav",".m4a",".flac")

videos = [
    os.path.join(video_folder, f)
    for f in os.listdir(video_folder)
    if f.lower().endswith(allowed_extensions)
]

if len(videos) == 0:
    print("No media file found")
    exit()

latest_video = max(videos, key=os.path.getctime)

video_path = latest_video

audio_file = "temp_audio.wav"

print("\nExtracting audio...")

subprocess.run([
    "ffmpeg",
    "-i", video_path,
    "-ar", "16000",
    "-ac", "1",
    "-vn",
    audio_file
])

print("Transcribing...")

result = model.transcribe(audio_file)
transcript = result["text"]

print("\nTranscript:\n")
print(transcript)

output_path = os.path.join(video_folder, "output.txt")

with open(output_path, "w", encoding="utf-8") as f:
    f.write(transcript)

summarizer = pipeline("summarization", model="facebook/bart-large-cnn")


from transformers import pipeline
import os

print("Generating summary...")

video_folder = "uploads"

output_path = os.path.join(video_folder, "output.txt")
summary_path = os.path.join(video_folder, "summary.txt")

with open(output_path, "r", encoding="utf-8") as f:
    transcript = f.read()

summarizer = pipeline("summarization")

# Step 1: Split transcript
max_chunk = 900
chunks = [transcript[i:i+max_chunk] for i in range(0, len(transcript), max_chunk)]

chunk_summaries = []

# Step 2: Summarize each chunk
for chunk in chunks:
    summary = summarizer(
        chunk,
        max_length=150,
        min_length=60,
        do_sample=False
    )
    chunk_summaries.append(summary[0]["summary_text"])

# Step 3: Combine summaries
combined_summary = " ".join(chunk_summaries)

# Step 4: Final summarization
final_summary = summarizer(
    combined_summary,
    max_length=200,
    min_length=100,
    do_sample=False
)[0]["summary_text"]

with open(summary_path, "w", encoding="utf-8") as f:
    f.write(final_summary)

print("Summary generated successfully")

with open(summary_path, "w", encoding="utf-8") as f:
    f.write(summary[0]['summary_text'])
os.remove(audio_file)

print("\nTranscript saved to output.txt")
print("Summary saved to summary.txt")

import subprocess

subprocess.run(["python", "quiz.py"])