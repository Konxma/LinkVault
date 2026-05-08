package com.konxma.linkvault.service;

import com.konxma.linkvault.model.Link;
import com.konxma.linkvault.repository.LinkRepository;
import java.util.List;

public class LinkService {
  private final LinkRepository linkRepository;

  public LinkService() {
    this.linkRepository = new LinkRepository();
  }

  public List<Link> getLinksByCategory(int categoryId) {
    return linkRepository.getLinksByCategoryId(categoryId);
  }

  public boolean addLinkWithTags(Link link, String tags) {
    return linkRepository.addLinkWithTags(link, tags);
  }

  public void updateLink(Link link) {
    linkRepository.updateLink(link);
  }

  public boolean deleteLink(int linkId) {
    return linkRepository.deleteLink(linkId);
  }
}