#include <dlfcn.h>
#include <sys/system_properties.h>
#include <cstdint>
#include <jni.h>

static void* libc;
static int (*property_foreach)(
        void (*callback)(const prop_info* pi, void* cookie),
        void* cookie);
static int (*property_read)(
        const prop_info* pi,
        char* name, char* value);
static void (*property_read_callback)(
        const prop_info* pi,
        void (*callback)(void* cookie, const char* name, const char* value, uint32_t serial),
        void* cookie);
static const prop_info* (*property_find_nth)(unsigned n);

static void
get_from_libc(const char* fn_name, void* fn_ptr) {
    void** fn_ptr_ptr = static_cast<void **>(fn_ptr);
    if (*fn_ptr_ptr == nullptr) {
        if (!libc) {
            libc = dlopen("libc.so", RTLD_LAZY);
            if (libc == nullptr) return;
        }
        *fn_ptr_ptr = dlsym(libc, fn_name);
    }
}

static char gName[PROP_NAME_MAX];
static char gValue[PROP_VALUE_MAX];

struct JNICookie {
    _JNIEnv *env;
    jclass clazz;
    jobject callback;
};

static void
handle_property(void* cookie, const char* name, const char* value, uint32_t  __unused serial) {
    auto *jniCookie = static_cast<struct JNICookie *>(cookie);
    _JNIEnv *env = jniCookie->env;
    jmethodID handleProperty = env->GetMethodID(jniCookie->clazz,
                                                "handleProperty",
                                                "(Ljava/lang/String;Ljava/lang/String;)V");
    env->CallVoidMethod(jniCookie->callback,
                        handleProperty,
                        env->NewStringUTF(name),
                        env->NewStringUTF(value));
}

static void
handle_property(const prop_info *propInfo, void *cookie) {
    if (propInfo == nullptr) return;
    get_from_libc("__system_property_read_callback", &property_read_callback);
    if (property_read_callback == nullptr) return;
    property_read_callback(propInfo, handle_property, cookie);
}

extern "C" JNIEXPORT void JNICALL
Java_io_github_ferreol_seteditplus_Native_readAndroidPropertiesPost26(_JNIEnv *env, jclass clazz, jobject property_callback) {
    get_from_libc("__system_property_foreach", &property_foreach);
    if (property_foreach == nullptr) return;
    struct JNICookie jniCookie = {
            .env = env,
            .clazz = clazz,
            .callback = property_callback
    };
    while (property_foreach(handle_property, &jniCookie) == 1);
}


extern "C" JNIEXPORT jboolean JNICALL
Java_io_github_ferreol_seteditplus_Native_readAndroidPropertyPre26(_JNIEnv *env, jclass __unused clazz, jint n, jobjectArray property) {
    get_from_libc("__system_property_find_nth", &property_find_nth);
    if (property_find_nth == nullptr) return 0;
    const prop_info *propInfo = property_find_nth(n);
    if (propInfo == nullptr) return 0;
    get_from_libc("__system_property_read", &property_read);
    if (property_read == nullptr) return 0;
    property_read(propInfo, gName, gValue);
    env->SetObjectArrayElement(property, 0, env->NewStringUTF(gName));
    env->SetObjectArrayElement(property, 1, env->NewStringUTF(gValue));
    return 1;  // true
}
