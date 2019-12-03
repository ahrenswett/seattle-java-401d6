# Lecture 26: Android Activities, Layouts and Event Handlers

## Agenda
- Announcements
    - Career Coaching Workshop is NOT this Friday
        - Combining workshop 2 and workshop 3 into a single day on Nov 8
        - Prep assignments are still spread out
    - Feedback review
- Code review: Midterm Projects
    - What we've learned
    - What we're going to learn
- Lecture
    - Android Studio intro/reminders & WYSIWYG editor
    - Event listeners: they're the same as in JS
    - Activity instance methods and Intents
- Lab intro


## Feedback Review

### The Good/Most Helpful

- Labs to look back on
- Instructors being available/helpful during project week
    - <3

### The Constructive/Least Helpful

- Spring Auth is annoying, don't know the details of how it works/is put together
    - realistically, that's fine
    - we'll see a bit more with Cognito later, but... implementing a login system from scratch is usually not the right answer.
- Projects were hard but rewarding
    - excellent
- Hard to find time for meetups
    - Usually, meetups are a good use of time--if you need to leave lecture or code challenge time early, I'll generally support you!

## Warmup Question
1. You're writing frontend JavaScript, and you want to console.log when a button is clicked. What do you need to do?

### Creating a new project
1. Open Android Studio
2. Choose "Start a new Android Studio project"
3. Choose an Application name
4. Leave the company domain as default
5. Choose a Project Location (make sure you create a folder to contain everything)
6. Leave everything else default
7. Press Next
8. Select the checkbox for "Phone and Tablet", then choose API level 23
9. Press Next
10. Choose empty-actvity
11. Press Next
12. Leave the Activity Name and Layout Name as defaults
13. Press Finish

Observe Android Studio and Gradle generating an Android project for you.
Notice that it built a directory structure like
`app > java > com.example.username.application > MainActivity`.
MainActivity defines the first thing that will run when someone starts your
app, like a `Main` method in a traditional Java program.

Look at the directory structure outside of `app > java` and observe what's
put under `app > res > layout`. This is where the templates for Android
applications exist. Notice the file `activity_main.xml` defines what's
shown on the screen when your app is run.

## Android Studio Layout
You should be familiar with the Android Studio layout as it is very similar
to IntelliJ IDEA.

Now we will go over a quick breakdown of each `Tool Window`
* Lefthand side - Project Tool Window
* Main View - Editor

### Event Listeners

Add a button that can update the text of a text view on the page.

### Android Emulators
Begin the process of running the application. Note that you have the ability to run on multiple phone types in the emulator, and you should definitely do this: your app needs to work on a variety of device types and sizes.

### Activity Lifecycle
Create a new Activity subclass called `LifeCycleActivity` and implement the following code:
```java
private static final String TAG = "LifecycleActivity";

@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "onCreate Called");
}

@Override
protected void onStart() {
    super.onStart();

    Log.d(TAG, "onStart Called");
}

@Override
protected void onResume() {
    super.onResume();

    Log.d(TAG, "onResume Called");
}

@Override
protected void onPause() {
    super.onPause();

    Log.d(TAG, "onPause Called");
}

@Override
protected void onStop() {
    super.onStop();

    Log.d(TAG, "onStop called");
}

@Override
protected void onRestart() {
    super.onRestart();

    Log.d(TAG, "onRestart: called");
}

@Override
protected void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "onDestroy: called");
}
```

Then, navigate to the `HomeActivity` and change it to `extend LifeCycleActivity`.

Run the application to and show accessing `LogCat` and the lifecycle methods being called.


### Android Studio
* Get students familiar with the Android Studio Ecosystem.
  * Installing Toolchains
  * Compiling your first program
  * Familiar with Dev environment
