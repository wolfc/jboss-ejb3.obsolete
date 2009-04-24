install:
	sudo install --no-target-directory --preserve-timestamps --group=svnsync --owner=svnsync fix_rev.sh /var/svn/anonsvn.jboss.org-jbossas/hooks/post-commit

svnserve:
	sudo install --no-target-directory --preserve-timestamps svn.xinetd /etc/xinetd.d/svn
