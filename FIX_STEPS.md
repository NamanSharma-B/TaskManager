# Fix for JavaPoet ClassName.canonicalName() Error

## Problem
The error `Unable to find method 'java.lang.String com.squareup.javapoet.ClassName.canonicalName()'` occurs due to a version mismatch between the Navigation SafeArgs plugin and JavaPoet dependencies.

## Root Cause
The Navigation SafeArgs plugin uses JavaPoet for code generation, but the current dependency versions have incompatibility issues where the method `canonicalName()` is missing from the version of JavaPoet being used.

## Solution Applied

### 1. **Updated Build Configuration**
Modified `/build.gradle.kts` to:
- Add KSP, Hilt, and Navigation SafeArgs plugins at the root level
- Force JavaPoet to version 1.13.0 which has the required `canonicalName()` method
- Added dependency substitution for additional safety

### 2. **Steps to Resolve**

#### Option A: Quick Fix (Recommended)
1. **Invalidate Cache and Restart IDE**
   - In Android Studio: File → Invalidate Caches → Invalidate and Restart
   - This clears the corrupted Gradle cache

2. **Stop All Gradle Daemons**
   ```bash
   cd /home/namansharma/AndroidStudioProjects/TaskManager
   ./gradlew --stop
   ```

3. **Clean and Sync**
   ```bash
   ./gradlew clean
   ./gradlew sync
   ```

#### Option B: Complete Clean Rebuild
1. Delete the Gradle cache:
   ```bash
   rm -rf ~/.gradle
   ```

2. Delete build directories:
   ```bash
   cd /home/namansharma/AndroidStudioProjects/TaskManager
   rm -rf build/
   rm -rf app/build/
   ```

3. Sync Gradle:
   ```bash
   ./gradlew --refresh-dependencies sync
   ```

#### Option C: Using Android Studio UI
1. File → Invalidate Caches → Invalidate and Restart
2. Build → Clean Project
3. Build → Rebuild Project
4. File → Sync with Gradle Files

## Files Modified

1. **build.gradle.kts** - Added dependency resolution and plugin management
2. **gradle/libs.versions.toml** - Ensured navigation-safeargs version matches navigation version

## Key Dependencies
- Gradle: 8.13
- AGP (Android Gradle Plugin): 8.8.2
- Kotlin: 2.0.21
- Navigation: 2.8.9
- JavaPoet: 1.13.0 (forced)

## Verification
After applying the fix, you should be able to:
- Build the project successfully
- Run tests without ClassName.canonicalName() errors
- Use Navigation SafeArgs without issues

## If Problem Persists
1. Ensure you're using Java 11 or higher (your project targets Java 11)
2. Check if there are other third-party plugins that might conflict
3. Try using a different IDE or updating Android Studio to the latest version
4. Create a new project and migrate if necessary

