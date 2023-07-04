#include "vmlinux.h"

#include <bpf/bpf_tracing.h>
#include <bpf/bpf_core_read.h>
#include <bpf/bpf_helpers.h>


char LICENSE[] SEC("license") = "GPL";

volatile int tty_ino = -1;

extern int bpf_tty_read_in_kmod(char ch) __ksym;

SEC("fexit/tty_read")
int BPF_PROG(on_tty_read_exit, struct kiocb *iocb, struct iov_iter *to, int ret){
  if (BPF_CORE_READ(iocb, ki_filp, f_inode, i_ino) == tty_ino){
    char ch;
    bpf_probe_read_user(&ch, 1, to->ubuf);
    bpf_tty_read_in_kmod(ch);
  }
  return 0;
}
