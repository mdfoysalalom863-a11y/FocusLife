# FocusLife Android App (V1.0)

## Android ফোন থেকে APK তৈরি
1. ZIP ফাইলটি ডাউনলোড করে Extract করো।
2. GitHub-এ `FocusLife` নামে নতুন repository তৈরি করো।
3. Extract করা ফোল্ডারের ভেতরের সব ফাইল repository-র root-এ upload করো। `.github/workflows/android-build.yml` ফাইলটিও রাখতে হবে।
4. GitHub repository-তে Actions tab খুলে `Build FocusLife APK` workflow নির্বাচন করো।
5. `Run workflow` চাপো। Build সফল হলে workflow run-এর নিচে `FocusLife-APK` artifact থেকে APK ডাউনলোড করো।

## বর্তমান সংস্করণ
- Dark theme এবং বাংলা/English toggle
- Dashboard, Tasks, Notes, Habits, Pomodoro focus timer
- Tasks/Notes/Habits ফোনে SharedPreferences-এ সংরক্ষিত
- GitHub Actions debug APK workflow

## পরবর্তী কাজ
Calendar, notification reminders, database backup/export, home-screen widget এবং secure online AI backend পরবর্তী ধাপে যুক্ত হবে।
