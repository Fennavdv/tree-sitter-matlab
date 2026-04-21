
#include <jni.h>
void *tree_sitter_matlab();
/*
 * Class:     org_treesitter_TreeSitterMatlab
 * Method:    tree_sitter_matlab
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_treesitter_TreeSitterMatlab_tree_1sitter_1matlab
  (JNIEnv *env, jclass clz){
   return (jlong) tree_sitter_matlab();
}
