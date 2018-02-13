kernel void nabeatsu(global int *a){
  int i = get_global_id(0);
  int v = i + 1;

  if(v % 3 == 0){
    a[i] = 3;
    return;
  }
  else{
    do{

      if(v % 10 == 3){
        a[i] = 3;
        return;
      }

      v /= 10;
    } while(v > 0);
  }

  a[i] = 0;
}
