# Deep Link Extraction Project

### IMPORTANT
**- When interacting with the application, user needs to make sure all of the permissions are provided inside the Manifest.xml. Otherwise the application won't work.**

**- Application will work only when us installed on the device, where application that needs to be tested are installed.**

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
   - Android prevents direct access to the manifest file of another app, making it difficult to extract deep links within the app.
   
2. **Need for External Tools**:
   - To extract and parse the `AndroidManifest.xml` of another app, tools like `apktool` or `AXMLParser` would be necessary.

## Next Steps for Future Developers
1. **Evaluate the Approach**:
   - Decide whether the deep link extraction needs to be performed on-device or outside the Android environment (e.g., using `apktool` to decompile the APK and extract the manifest).
   
2. **Use External Libraries**:
   - Investigate libraries such as `AXMLParser` to parse the `AndroidManifest.xml` on-device if that remains a requirement.

## Current Code State
- **DeepLinksActivity.java**: 
   - Contains the logic for attempting to extract deep links from activities within the target application.
   - Due to Android restrictions, the current implementation will not work for parsing another app's manifest.
- **IPCActivity.java and IPCAdapter.java**: 
   - These files focus on Inter-Process Communication (IPC) components of the app.

## How IPC Components Work
This section provides an overview of how the Inter-Process Communication (IPC) components of the app work, including activities, services, content providers, and broadcast receivers. The IPC components are displayed in a `RecyclerView` and allow users to interact with them.

### Key Classes
1. **IPCActivity.java**:
   - This class is responsible for listing and interacting with various IPC components (activities, services, content providers, and broadcast receivers) of a selected package.
   - It retrieves exported IPC components from the package using `PackageManager` and displays them in a `RecyclerView`.
   - Users can interact with these components (e.g., launch an activity, start a service) by clicking on them in the list.

2. **IPCAdapter.java**:
   - This adapter manages the data and display of IPC components in the `RecyclerView`.
   - Each component (activity, service, content provider, or broadcast receiver) is displayed as a colored item (e.g., blue for activities, green for services).
   - When an item is clicked, the corresponding action (e.g., starting an activity or service) is triggered based on the type of IPC component.

### How It Works
- **Component Fetching**: 
   - `IPCActivity.java` uses `PackageManager` to retrieve all the exported activities, services, content providers, and broadcast receivers from the specified package.
   - The components are filtered to show only those that are exported and are not part of the main activity.

- **Component Display**: 
   - The retrieved IPC components are displayed in a `RecyclerView` with different colors indicating the type of component (e.g., blue for activities, green for services).
   - `SpannableString` is used to apply these colors to the text.

- **Interaction**: 
   - Users can click on any IPC component in the list. Depending on the type of component, different actions are triggered:
     - **Activities**: The user is prompted with options to start the activity.
     - **Services**: The user is prompted to input parameters before starting the service.
     - **Content Providers**: The app attempts to discover accessible paths in the content provider using a wordlist of around 500 000 words.
     - **Broadcast Receivers**: The user is prompted to provide broadcast parameters.

### Extending IPC Functionality
- **Adding New Components**: To support additional IPC components, extend the logic in `IPCActivity.java` to retrieve and handle the new component type.
- **Custom Actions**: Customize the actions that are triggered when a user interacts with an IPC component by modifying the `onItemClick()` method in `IPCActivity.java`.

## Conclusion
Due to Android's security restrictions, extracting deep links directly from another app's manifest will require a more sophisticated approach, possibly involving external tools. Future developers will need to decide whether to continue attempting this on-device or offload this task to an external service.
