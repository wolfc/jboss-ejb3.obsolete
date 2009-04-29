#install: /var/svn/anonsvn.jboss.org-jbossas/hooks/post-commit
install: /home/svnsync/bin/fix_rev.sh /home/svnsync/bin/svnsync_and_fix.sh

/home/svnsync/bin/%: %
	mkdir -p /home/svnsync/bin
	cp -a $< $@

/var/svn/anonsvn.jboss.org-jbossas/hooks/post-commit: fix_rev.sh
	sudo install --no-target-directory --preserve-timestamps --group=svnsync --owner=svnsync fix_rev.sh /var/svn/anonsvn.jboss.org-jbossas/hooks/post-commit

svnserve:
	sudo install --no-target-directory --preserve-timestamps svn.xinetd /etc/xinetd.d/svn
