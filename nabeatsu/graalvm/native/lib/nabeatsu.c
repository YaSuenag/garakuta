int is_aho(int val){

  if(val % 3 == 0){
    return 1;
  }
  else{
    do{
      if(val % 10 == 3){
        return 1;
      }
      val /= 10;
    } while(val > 0);
  }

  return 0;
}
