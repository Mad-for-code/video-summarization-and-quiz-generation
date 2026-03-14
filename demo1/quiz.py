from transformers import pipeline
import os

print("Generating quiz...")

video_folder = "uploads"

transcript_path = os.path.join(video_folder, "output.txt")
summary_path = os.path.join(video_folder, "summary.txt")
quiz_path = os.path.join(video_folder, "quiz.txt")

if not os.path.exists(transcript_path):
    print("Transcript not found")
    exit()

if not os.path.exists(summary_path):
    print("Summary not found")
    exit()


with open(transcript_path, "r", encoding="utf-8") as f:
    transcript = f.read()

with open(summary_path, "r", encoding="utf-8") as f:
    summary = f.read()


# limit transcript for model
text = summary + transcript[:800]

generator = pipeline(
    "text2text-generation",
    model="google/flan-t5-base"
)

quiz_questions = []

for i in range(5):

    prompt = f"""
Create ONE multiple choice question from the following text.

Text:
{text}

Rules:
- Provide 4 options
- Mark the correct answer
- Make the question different from previous ones

Format:

Question:
A)
B)
C)
D)
Answer:
"""

    result = generator(prompt, max_length=200, do_sample=True)

    question = result[0]["generated_text"]

    quiz_questions.append(f"Question {i+1}:\n{question}\n")

quiz_text = "\n".join(quiz_questions)

with open(quiz_path, "w", encoding="utf-8") as f:
    f.write(quiz_text)

print("Quiz generated successfully")