# Simple javascript evaluator for android
This is a simple library to run javascript, Maybe this will help you more easily run javascript without using a library with a large size.

## Download [![JitPack](https://img.shields.io/github/tag/adi-itgg/AITJsEval.svg?label=JitPack)](https://jitpack.io/#adi-itgg/AITJsEval)
**Using gradle**
1. Add the JitPack repository to your build file.
Add it in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
2.Add the dependency
```gradle
	dependencies {
	    implementation 'com.github.adi-itgg:AITJsEval:v1.1'
	}
```
For less information, see maven/sbt/leiningen on jitpack.io [Documentation](https://jitpack.io/#adi-itgg/AITJsEval)

## Usage
Initialize instance AITJsEval first. Context must using Application Context!
```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize library first
        AITJsEval.initialize(applicationContext)
    }

}
```

## Evaluate JavaScript
```kotlin
   val sc  = """
     (function f() {
         var d = "EvalExample:10"
         var i = parseInt(d.split(":")[1]) + 2 * 88 
         return d.split(":")[0] + " " + i
      }());
   """
   AITJsEval.get().enqueue("sample", sc, object : OnJavaScriptResponseListener {
      override fun onResponse(script: Script) {
         rText.value = script.result
         Toast.makeText(context, "Script executed", Toast.LENGTH_SHORT).show()
      }
   })
```

## Proguard Rules
If you are using Proguard `minifyEnabled true`, Add this rules to your *proguard-rules.pro* file.
```pro
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
```
