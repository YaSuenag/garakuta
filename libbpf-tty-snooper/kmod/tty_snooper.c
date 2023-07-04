#include <linux/module.h>
#include <linux/btf.h>
#include <linux/tty.h>

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Yasumasa Suenaga");
MODULE_DESCRIPTION("TTY snooper for BPF");

BTF_SET8_START(bpf_tty_wrapper_ids)
BTF_ID_FLAGS(func, bpf_tty_read_in_kmod, KF_TRUSTED_ARGS)
BTF_SET8_END(bpf_tty_wrapper_ids)

static const struct btf_kfunc_id_set kfunc_set = {
  .owner = THIS_MODULE,
  .set   = &bpf_tty_wrapper_ids,
};


static int __init kfunc_init(void){
  return register_btf_kfunc_id_set(BPF_PROG_TYPE_TRACING, &kfunc_set);
}

__bpf_kfunc int bpf_tty_read_in_kmod(char ch){
  printk("DEBUG: %c\n", ch);
  return 0;
}


EXPORT_SYMBOL_GPL(bpf_tty_read_in_kmod);

module_init(kfunc_init);
