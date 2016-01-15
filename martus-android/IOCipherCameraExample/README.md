
IOCipher Camera Example
================

This is an example app for the IOCipher framework, which provides encrypted virtual disks for Android. 

This example demonstrates how to capture a still photo (as an in memory byte[] array) directly from the Camera sensor, store that as an encrypted JPEG file directly inside of IOCipher, and then later share that file as a in-memory byte[] or a stream directly from a ContentProvider. It also handles recording video with audio in the same manner, using the MJPEG MP4 Format and Raw PCM audio.

The example also provides two basic media viewer activities for photos and video. This provides an in-app mechanism for securely displaying media stored in IOCipher, without having to export the media to a file or share it with another app. These are very simple activities for now, but demonstrate the basic premise of how to extract and render media data from IOCipher. 

This example also uses the CacheWord library at https://github.com/guardianproject/cacheword to manage keys and passphrases.

Finally, all media processing is done using the latest code from JCodec at http://jcodec.org/ 

Please report bugs here:
https://dev.guardianproject.info/projects/iocipher/issues

Find out all about IOCipher here:
https://guardianproject.info/code/iocipher/

We'd like to hear from you if you have included IOCipher in your app!
support@guardianproject.info

MjpegView code originally sourced from various samples and threads. More information at:
https://github.com/elleryq/MjpegSample

SVG-Android library available at:
https://github.com/pents90/svg-android

Some icons courtesy of: http://www.pelfusion.com/
and http://www.iconarchive.com/show/flat-folder-icons-by-pelfusion/Empty-Folder-icon.html
(free for non-commercial use)
