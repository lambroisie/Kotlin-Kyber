#include <jni.h>
#include <string.h>
#include <stdlib.h>
#include "mlkem/mlkem_native.h"

// Sizes for ML-KEM-1024 as defined by the header given the CMake definition
#define PK_SIZE MLKEM1024_PUBLICKEYBYTES
#define SK_SIZE MLKEM1024_SECRETKEYBYTES
#define CT_SIZE MLKEM1024_CIPHERTEXTBYTES
#define SS_SIZE MLKEM_BYTES

/**
 * Generates a keypair and returns both in a single byte array [PK | SK]
 */
JNIEXPORT jbyteArray JNI_GenerateKeyPair(JNIEnv *env) {
    unsigned char pk[PK_SIZE];
    unsigned char sk[SK_SIZE];

    // Use the namespaced keypair function
    if (MLK_API_NAMESPACE(keypair)(pk, sk) != 0) {
        return NULL;
    }

    jbyteArray result = (*env)->NewByteArray(env, PK_SIZE + SK_SIZE);
    (*env)->SetByteArrayRegion(env, result, 0, PK_SIZE, (jbyte*)pk);
    (*env)->SetByteArrayRegion(env, result, PK_SIZE, SK_SIZE, (jbyte*)sk);

    return result;
}

/**
 * Encapsulates: generates a shared secret and a ciphertext using the public key.
 * Returns: byte array of size [CT | SS]
 */
JNIEXPORT jbyteArray JNI_Encapsulate(JNIEnv *env, jbyteArray pk_array) {
    unsigned char pk[PK_SIZE];
    unsigned char ct[CT_SIZE];
    unsigned char ss[SS_SIZE];

    jbyte* pk_ptr = (*env)->GetByteArrayElements(env, pk_array, NULL);
    memcpy(pk, pk_ptr, PK_SIZE);
    (*env)->ReleaseByteArrayElements(env, pk_array, pk_ptr, JNI_ABORT);

    // Use the namespaced encaps function
    if (MLK_API_NAMESPACE(enc)(ct, ss, pk) != 0) {
        return NULL;
    }

    jbyteArray result = (*env)->NewByteArray(env, CT_SIZE + SS_SIZE);
    (*env)->SetByteArrayRegion(env, result, 0, CT_SIZE, (jbyte*)ct);
    (*env)->SetByteArrayRegion(env, result, CT_SIZE, SS_SIZE, (jbyte*)ss);

    return result;
}

/**
 * Decapsulates: recovers the shared secret from the ciphertext using the secret key.
 */
JNIEXPORT jbyteArray JNI_Decapsulate(JNIEnv *env, jbyteArray ct_array, jbyteArray sk_array) {
    unsigned char ct[CT_SIZE];
    unsigned char sk[SK_SIZE];
    unsigned char ss[SS_SIZE];

    jbyte* ct_ptr = (*env)->GetByteArrayElements(env, ct_array, NULL);
    memcpy(ct, ct_ptr, CT_SIZE);
    (*env)->ReleaseByteArrayElements(env, ct_array, ct_ptr, JNI_ABORT);

    jbyte* sk_ptr = (*env)->GetByteArrayElements(env, sk_array, NULL);
    memcpy(sk, sk_ptr, SK_SIZE);
    (*env)->ReleaseByteArrayElements(env, sk_array, sk_ptr, JNI_ABORT);

    // Use the namespaced decaps function
    if (MLK_API_NAMESPACE(dec)(ss, ct, sk) != 0) {
        return NULL;
    }

    jbyteArray result = (*env)->NewByteArray(env, SS_SIZE);
    return result;
}
