#!/usr/bin/perl

use CGI;
use DBI;
use DBD::mysql;

print qq(Content-type: text/plain\n);
print qq(\n);

my $dbh=DBI->connect("DBI:mysql:database=emcmarket;host=localhost", "emcmarket", "NE6xYRe6UPwW3Ray", {AutoCommit => 0 });

my $cgi=CGI->new;
my $separator=":";
if ($cgi->param("clientversion") ne "") {
	$separator="|";
}
my $rows=0;
my $sth=$dbh->prepare(qq(
	select server, x, y, z, amount, buy, sell, owner, item, resnumber, chooseposition, lastseen, todelete
	from signs
	where (server like 'SMP_' or server = 'UTOPIA')
	and owner <> '' and owner is not NULL
	order by server, owner, x, z, y
));
$sth->execute();
my $rows=0;
while (my(@f)=$sth->fetchrow_array()) {
	$f[12]=($f[12] == 0 ? "" : "todelete");
	print join($separator, @f), "\n";
	$rows++;
}

open(F, ">>/tmp/downloaders.dat");
print F scalar localtime, "  ",  $rows ," downloaded by ", $cgi->param("name"), " client ", $cgi->param("clientversion"), "\n";
close F;
