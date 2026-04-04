import { useEffect, useState } from 'react';
import { getIncomingTransfers, getOutgoingTransfers } from './transferApi';

const defaultPageInfo = {
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  isFirst: true,
  isLast: true,
};

const normalizePage = (data, page) => ({
  content: data?.content || [],
  pageInfo: {
    currentPage: data?.number ?? page,
    totalPages: data?.totalPages ?? 0,
    totalElements: data?.totalElements ?? 0,
    isFirst: data?.first ?? page === 0,
    isLast: data?.last ?? true,
  },
});

const getErrorMessage = (err, fallbackMessage) => {
  if (typeof err === 'string') return err;
  if (err?.message) return err.message;
  if (err?.error) return err.error;
  return fallbackMessage;
};

export const useTransferLists = () => {
  const [incomingPage, setIncomingPage] = useState(0);
  const [outgoingPage, setOutgoingPage] = useState(0);

  const [incomingTransfers, setIncomingTransfers] = useState([]);
  const [outgoingTransfers, setOutgoingTransfers] = useState([]);

  const [incomingPageInfo, setIncomingPageInfo] = useState(defaultPageInfo);
  const [outgoingPageInfo, setOutgoingPageInfo] = useState(defaultPageInfo);

  const [incomingLoading, setIncomingLoading] = useState(false);
  const [outgoingLoading, setOutgoingLoading] = useState(false);

  const [incomingError, setIncomingError] = useState(null);
  const [outgoingError, setOutgoingError] = useState(null);

  const loadIncoming = async (page = incomingPage) => {
    setIncomingLoading(true);
    setIncomingError(null);
    try {
      const data = await getIncomingTransfers({ page, size: 10, sort: ['createdAt,desc'] });
      const normalized = normalizePage(data, page);
      setIncomingTransfers(normalized.content);
      setIncomingPageInfo(normalized.pageInfo);
    } catch (err) {
      setIncomingError(getErrorMessage(err, 'Error al cargar transferencias entrantes'));
      setIncomingTransfers([]);
      setIncomingPageInfo(defaultPageInfo);
    } finally {
      setIncomingLoading(false);
    }
  };

  const loadOutgoing = async (page = outgoingPage) => {
    setOutgoingLoading(true);
    setOutgoingError(null);
    try {
      const data = await getOutgoingTransfers({ page, size: 10, sort: ['createdAt,desc'] });
      const normalized = normalizePage(data, page);
      setOutgoingTransfers(normalized.content);
      setOutgoingPageInfo(normalized.pageInfo);
    } catch (err) {
      setOutgoingError(getErrorMessage(err, 'Error al cargar transferencias salientes'));
      setOutgoingTransfers([]);
      setOutgoingPageInfo(defaultPageInfo);
    } finally {
      setOutgoingLoading(false);
    }
  };

  useEffect(() => {
    loadIncoming(incomingPage);
  }, [incomingPage]);

  useEffect(() => {
    loadOutgoing(outgoingPage);
  }, [outgoingPage]);

  return {
    incomingTransfers,
    outgoingTransfers,
    incomingPageInfo,
    outgoingPageInfo,
    incomingLoading,
    outgoingLoading,
    incomingError,
    outgoingError,
    incomingPage,
    outgoingPage,
    setIncomingPage,
    setOutgoingPage,
    loadIncoming,
    loadOutgoing,
  };
};
