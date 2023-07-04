#include <sys/stat.h>
#include <stdio.h>
#include <unistd.h>
#include <bpf/libbpf.h>
#include "tty_read.skel.h"


static ino_t tty_ino;
static struct tty_read_bpf *skel;


static int libbpf_print_fn(enum libbpf_print_level level, const char *format, va_list args){
  return vfprintf(stderr, format, args);
}

static int setup_tty(const char *ttypath){
  struct stat statst;
  if(stat(ttypath, &statst) == -1){
    perror("stat");
    return -1;
  }
  tty_ino = statst.st_ino;
  return 0;
}

static int setup_bpf(){
  int ret;

  libbpf_set_print(libbpf_print_fn);

  skel = tty_read_bpf__open_and_load();
  if (!skel) {
    fprintf(stderr, "Could not load BPF program\n");
    return -1;
  }

  skel->data->tty_ino = tty_ino;

  ret = tty_read_bpf__attach(skel);
  if(ret != 0){
    fprintf(stderr, "Could not attach BPF skeleton (%d)\n", ret);
    return -2;
  }

  return 0;
}

int main(int argc, char *argv[]){
  char ch;

  if(setup_tty(argv[1]) != 0){
    return -1;
  }
  if(setup_bpf() != 0){
    return -2;
  }

  printf("Snooping %s (ino=%lu)\n", argv[1], tty_ino);
  printf("Press Enter to exit...");
  fflush(stdout);
  read(STDIN_FILENO, &ch, 1);

  tty_read_bpf__destroy(skel);
  return 0;
}
