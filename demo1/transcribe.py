import whisper
import subprocess
from transformers import pipeline
import os
import sys

# ✅ GET FILENAME FROM JAVA
file_name = sys.argv[1]

upload_dir = "C:/Users/HP/Downloads/demo1/demo1/uploads/"

video_path = os.path.join(upload_dir, file_name)

# ✅ UNIQUE OUTPUT FILES PER VIDEO
transcript_file = os.path.join(upload_dir, f"{file_name}_output.txt")
summary_file = os.path.join(upload_dir, f"{file_name}_summary.txt")
quiz_file = os.path.join(upload_dir, f"{file_name}_quiz.txt")

audio_file = os.path.join(upload_dir, f"{file_name}_audio.wav")

print("Processing:", video_path)

# ✅ LOAD MODELS (ONLY ONCE)
model = whisper.load_model("base")
summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

# =========================
# 🎧 STEP 1: EXTRACT AUDIO
# =========================
print("\nExtracting audio...")

subprocess.run([
    "ffmpeg",
    "-i", video_path,
    "-ar", "16000",
    "-ac", "1",
    "-vn",
    audio_file
])

# =========================
# 📝 STEP 2: TRANSCRIPTION
# =========================
print("Transcribing...")

result = model.transcribe(audio_file)
transcript = result["text"]

with open(transcript_file, "w", encoding="utf-8") as f:
    f.write(transcript)

print("Transcript saved")

# =========================
# 📄 STEP 3: SUMMARY
# =========================
print("Generating summary...")

# Split into chunks (important for long text)
max_chunk = 900
chunks = [transcript[i:i+max_chunk] for i in range(0, len(transcript), max_chunk)]

chunk_summaries = []

for chunk in chunks:
    try:
        summary = summarizer(
            chunk,
            max_length=120,
            min_length=40,
            do_sample=False
        )
        chunk_summaries.append(summary[0]["summary_text"])
    except:
        continue

combined_summary = " ".join(chunk_summaries)

final_summary = summarizer(
    combined_summary,
    max_length=180,
    min_length=80,
    do_sample=False
)[0]["summary_text"]

with open(summary_file, "w", encoding="utf-8") as f:
    f.write(final_summary)

print("Summary saved")

# =========================
# 🧠 STEP 4: QUIZ
# =========================
print("Generating quiz...")

quiz_prompt = f"""
Generate 5 MCQ questions from the following text.

Rules:
- Each question must have 4 options (A, B, C, D)
- Mention correct answer
- Questions must be meaningful

Text:
{final_summary}
"""

generator = pipeline("text-generation", model="gpt2")

quiz_output = generator(
    quiz_prompt,
    max_length=500,
    num_return_sequences=1
)[0]["generated_text"]

with open(quiz_file, "w", encoding="utf-8") as f:
    f.write(quiz_output)

print("Quiz saved")

# =========================
# 🧹 CLEANUP
# =========================
if os.path.exists(audio_file):
    os.remove(audio_file)

print("\n✅ ALL TASKS COMPLETED")