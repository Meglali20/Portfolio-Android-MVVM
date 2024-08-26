# Portfolio Android app
A portfolio app based on MVVM architecture, custom views and Firebase.
<p align="center">
<img src="/screenshots/app_light.gif" width="33%"/>
<img src="/screenshots/app_night.gif" width="33%"/>
</p>

## Features

- **Architecture:** MVVM (Model-View-ViewModel)
- **Networking:** Retrofit2, OkHttp3 & Moshi
- **Database:** Room
- **Dependency Injection:** Hilt
- **Image Loading:** Glide
- **Jetpack Components:** Lifecycle, ViewModel, DataBinding, DataStore
- **Firebase Integration:** Utilizing Firebase as a REST API
- **LeakCanary:** A memory leak detection library
- **Custom Views:**

  - [WavesTextView](#wavestextview) reproduces the animation effect used on [Midjourney's](https://midjourney.com) landing page.
  - [TextShufflerView](#textshufflerview) (View and Jetpack Compose) Inspired by [TypeShuffleAnimation](https://tympanus.net/Development/TypeShuffleAnimation/).
  - [BottomNavigationBar](#bottomnavigationbar) mixes TextShufflerView with some animated drawable effects.
  - [ImagePiecesView](#imagepiecesview) Inspired by [AnimatedImagePieces](https://tympanus.net/Development/AnimatedImagePieces/).
  - [GLImageView](#glimageview) based on [Shadertoy](https://www.shadertoy.com/view/lssGDj) with some modifications.

## Multiple Language Support
The app supports multiple languages. To add data for each language, use the provided python code that would insert the data for each language and uploads the assets into Firebase (check the language_key.json.template file to see what you need to fill) each language must be in a json file in the lang directory, the admin directory would contain the serviceAccountKey for Firebase and a config.py containing DATABASE_URL and STORAGE_BUCKET


## WavesTextView
reproduces the animation effect used on [Midjourney's](https://midjourney.com) landing page.
<p align="center">
<img src="/screenshots/waves.gif" width="33%"/>
<img src="/screenshots/waves2.gif" width="33%"/>
</p>

## TextShufflerView 
Can be used in XML and a version for Jetpack Compose Inspired by [TypeShuffleAnimation](https://tympanus.net/Development/TypeShuffleAnimation/).
<p align="center">
<img src="/screenshots/text_shuffler.gif" width="33%"/>
<img src="/screenshots/text_shuffler2.gif" width="33%"/>
</p>

## BottomNavigationBar
a custom bottom navigation bar
Mixing TextShufflerView with some animated drawable effect
<p align="center">
<img src="/screenshots/bottom_nav_bar1.gif" width="33%"/>
<img src="/screenshots/bottom_nav_bar2.gif" width="33%"/>
</p>


## ImagePiecesView
Inspired by [AnimatedImagePieces](https://tympanus.net/Development/AnimatedImagePieces/).
<p align="center">
<img src="/screenshots/image_pieces1.gif" width="33%"/>
<img src="/screenshots/image_pieces2.gif" width="33%"/>
<img src="/screenshots/image_pieces3.gif" width="33%"/>
</p>

## GLImageView 
Based on [Shadertoy](https://www.shadertoy.com/view/lssGDj) with some modifications.
<p align="center">
<img src="/screenshots/glimage1.gif" width="33%"/>
<img src="/screenshots/glimage2.gif" width="33%"/>
</p>