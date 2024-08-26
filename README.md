# Deep Link Extraction Project

## Project Summary
The purpose of this project is to extract deep links from the `AndroidManifest.xml` file of a target application installed on the same device. The extracted deep links are then displayed to the user within a `RecyclerView`.

## Current Status
### Initial Attempts
1. **PackageInfo Approach**: 
   - We initially tried extracting `PackageInfo` using `PackageManager` and iterating over the activities to find deep links.
   - This approach was limited because it could not access the manifest file directly to pull out deep link data from `intent-filter` tags.

2. **Direct Manifest Parsing**:
   - We attempted to parse the `AndroidManifest.xml` of the target application using `XmlResourceParser`. 
   - This failed because Android does not permit direct access to the manifest file of another application using the `PackageManager`.
   - Errors occurred because necessary XML resources from the target application could not be retrieved.

### Challenges Faced
1. **Android Security Restrictions**:
   - Android prevents direct access to the manifest file of another app, making it difficult to programmatically extract deep links within the app.
   
2. **Need for External Tools**:
   - To extract and parse the `AndroidManifest.xml` of another app, tools like `apktool` or `AXMLParser` would be necessary. These tools are typically used outside the Android environment.

## Next Steps for Future Developers
1. **Evaluate the Approach**:
   - Decide whether the deep link extraction needs to be performed on-device or outside the Android environment (e.g., using `apktool` to decompile the APK and extract the manifest).
   
2. **Use External Libraries**:
   - Investigate libraries such as `AXMLParser` to programmatically parse the `AndroidManifest.xml` on-device if that remains a requirement.

3. **Possible Alternatives**:
   - If analyzing multiple apps is a requirement, consider building a server-side tool that receives APK files, extracts the manifest, and returns the deep links.
   - Alternatively, a CLI tool could be built to extract deep links from APKs using `apktool` and similar tools.

## Current Code State
- **DeepLinksActivity.java**: 
   - Contains the logic for attempting to extract deep links from activities within the target application.
   - Due to Android restrictions, the current implementation will not work for parsing another app's manifest.
- **IPCActivity.java and IPCAdapter.java**: 
   - These files focus on Inter-Process Communication (IPC) components of the app, but they were not the main focus as we shifted to handling deep links specifically.

## Conclusion
Due to Android's security restrictions, extracting deep links directly from another app's manifest will require a more sophisticated approach, possibly involving external tools. Future developers will need to decide whether to continue attempting this on-device or offload this task to an external service.
