void native(int val, void (*callback)(int arg)){
  callback(val + 1);
}
