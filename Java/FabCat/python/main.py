import speech_recognition as sr

r = sr.Recognizer()
with sr.Microphone() as source:
    audio = r.listen(source, phrase_time_limit=3)

text = r.recognize_google(audio, language="it-IT")
print (text)
