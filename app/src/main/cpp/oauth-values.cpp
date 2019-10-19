//
// Created by Nico Mexis on 19.10.2019.
//

#include <jni.h>

extern "C" {

JNIEXPORT jstring JNICALL
Java_studip_1uni_1passau_femtopedia_de_unipassaustudip_natives_OAuthValues_getConsumerKey(
        JNIEnv *env,
        jclass clazz) {
    return env->NewStringUTF("CONSUMER_KEY_HERE");
}

JNIEXPORT jstring JNICALL
Java_studip_1uni_1passau_femtopedia_de_unipassaustudip_natives_OAuthValues_getConsumerSecret(
        JNIEnv *env,
        jclass clazz) {
    return env->NewStringUTF("CONSUMER_SECRET_HERE");
}

}