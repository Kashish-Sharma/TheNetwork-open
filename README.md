# TheNetwork

Before you continue
------
* The App is still under development so this may contain some errors.<br>
* The Gradle file has been edited for security purpose. <br>
* Include your own project's gradle Fields in "google services.json" file. Without that the app wont run.<br>
* Generate and add your own places api key in the manifest accordingly ([link to the docs](https://developers.google.com/places/web-service/intro)).<br>
* Enable google signin and signin with email from your firebase dashboard.
* Deploy firebase cloud functions provided in cloud-functions folder ([Link to the docs](https://cloud.google.com/functions/docs/))

Description
---------
TheNetwork is a blog cum chat app. It's completely built using firebase.
Users can post, comment, like and bookmark the blogs, also users can send follow requests to connect with people.
Users can create events and also prepare an event roadmap.
Pagination for realtime data is also included in chats, blogs and events.
Notifications are also supported whenever a user sends a chat message or a follow request.

Screenshots
----------
* **Home**<br>
<p float="left">
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/Home.jpg" alt="Blogs" width="200dp" height="400dp">          
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/blogDetail.jpg" alt="Blog detail" width="200dp" height="400dp">
</p>

* **Events**<br>
<p float="left">
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/Events.jpg" alt="Events" width="200dp" height="400dp">          
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/EventDetail.jpg" alt="Event detail" width="200dp" height="400dp">
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/roadmapclosed.jpg" alt="Roadmap" width="200dp" height="400dp">          
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/roadmapopen.jpg" alt="Roadmap closed" width="200dp" height="400dp">
</p>

* **Chats**<br>
<p float="left">
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/chat.jpg" alt="Chats" width="200dp" height="400dp">          
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/chatdetail.jpg" alt="Chat detail" width="200dp" height="400dp">
</p>

* **Profile**<br>
<p float="left">
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/profilemy.jpg" alt="My profile" width="200dp" height="400dp">          
<img src="https://github.com/Kashish-Sharma/TheNetwork/blob/experimental/Screenshots/profileother.jpg" alt="Other profile" width="200dp" height="400dp">
</p>

External libraries used
----------
* [Material date time picker](https://github.com/wdullaer/MaterialDateTimePicker)
* [Expandable recyclerview](https://github.com/thoughtbot/expandable-recycler-view)
* [Ken Burns View](https://github.com/flavioarfaria/KenBurnsView)
* [Big Image Loader](https://github.com/Piasy/BigImageViewer)
* [Circular Image view](https://github.com/hdodenhof/CircleImageView)
* [Image cropper](https://github.com/ArthurHub/Android-Image-Cropper)
* [Glide](https://github.com/bumptech/glide)
* [Image compressor](https://github.com/zetbaitsu/Compressor)
